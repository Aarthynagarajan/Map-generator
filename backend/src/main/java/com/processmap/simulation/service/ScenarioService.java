package com.processmap.simulation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.dto.ScenarioRequestDTO;
import com.processmap.dto.ScenarioResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.history.entity.Diagram;
import com.processmap.history.repository.DiagramRepository;
import com.processmap.simulation.entity.Scenario;
import com.processmap.simulation.mapper.ScenarioMapper;
import com.processmap.simulation.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final DiagramRepository diagramRepository;
    private final ScenarioMapper scenarioMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ScenarioResponseDTO createDefaultScenario(UUID diagramId) {
        log.info("Creating default 'Normal Operation' scenario for diagram ID: {}", diagramId);
        Diagram diagram = diagramRepository.findById(diagramId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Diagram not found", HttpStatus.NOT_FOUND));

        // Read nodes from diagram graph snapshot to find stoppers and default them to "open"/"on"
        Map<String, String> defaultStoppers = new HashMap<>();
        JsonNode graph = diagram.getGraphSnapshot();
        if (graph != null && graph.has("nodes")) {
            for (JsonNode node : graph.get("nodes")) {
                String entityClass = node.path("data").path("entityClass").asText();
                if (isStopperClass(entityClass)) {
                    String defaultState = isElectricalClass(entityClass) ? "on" : "open";
                    defaultStoppers.put(node.get("id").asText(), defaultState);
                }
            }
        }

        JsonNode stopperStatesJson = objectMapper.valueToTree(defaultStoppers);

        Scenario scenario = Scenario.builder()
                .diagram(diagram)
                .name("Normal Operation")
                .stopperStates(stopperStatesJson)
                .isDefault(true)
                .build();

        Scenario saved = scenarioRepository.save(scenario);
        return scenarioMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ScenarioResponseDTO> listScenarios(UUID diagramId) {
        log.info("Listing scenarios for diagram ID: {}", diagramId);
        List<Scenario> scenarios = scenarioRepository.findByDiagramId(diagramId);
        return scenarios.stream().map(scenarioMapper::toResponseDTO).toList();
    }

    @Transactional
    public ScenarioResponseDTO createScenario(ScenarioRequestDTO request) {
        log.info("Creating custom scenario for diagram ID: {}", request.diagramId());
        Diagram diagram = diagramRepository.findById(request.diagramId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Diagram not found", HttpStatus.NOT_FOUND));

        JsonNode stopperStatesJson = objectMapper.valueToTree(request.stopperStates());

        Scenario scenario = Scenario.builder()
                .diagram(diagram)
                .name(request.name())
                .stopperStates(stopperStatesJson)
                .isDefault(request.isDefault())
                .build();

        Scenario saved = scenarioRepository.save(scenario);
        return scenarioMapper.toResponseDTO(saved);
    }

    @Transactional
    public ScenarioResponseDTO updateScenario(UUID id, ScenarioRequestDTO request) {
        log.info("Updating scenario ID: {}", id);
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Scenario not found", HttpStatus.NOT_FOUND));

        JsonNode stopperStatesJson = objectMapper.valueToTree(request.stopperStates());
        scenario.setName(request.name());
        scenario.setStopperStates(stopperStatesJson);
        scenario.setIsDefault(request.isDefault());

        Scenario saved = scenarioRepository.save(scenario);
        return scenarioMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteScenario(UUID id) {
        log.info("Deleting scenario ID: {}", id);
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Scenario not found", HttpStatus.NOT_FOUND));
        scenarioRepository.delete(scenario);
    }

    private boolean isStopperClass(String entityClass) {
        return Set.of(
            "GATE_VALVE", "BALL_VALVE", "BUTTERFLY_VALVE", "CHECK_VALVE",
            "CIRCUIT_BREAKER", "SWITCH", "RELAY", "LIMIT_SWITCH", "PUSH_BUTTON",
            "HYDRAULIC_VALVE", "CHECK_VALVE_HYD", "FLOW_CONTROL_VALVE", "DIRECTIONAL_CONTROL_VALVE"
        ).contains(entityClass.toUpperCase());
    }

    private boolean isElectricalClass(String entityClass) {
        return Set.of("CIRCUIT_BREAKER", "SWITCH", "RELAY", "PUSH_BUTTON").contains(entityClass.toUpperCase());
    }
}
