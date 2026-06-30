package com.processmap.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.ai.model.EntityEdge;
import com.processmap.ai.model.EntityGraph;
import com.processmap.dto.*;
import com.processmap.graph.model.DiagramGraph;
import com.processmap.graph.service.GraphService;
import com.processmap.graph.util.GraphSerializer;
import com.processmap.history.service.DiagramHistoryService;
import com.processmap.layout.model.LayoutGraph;
import com.processmap.layout.service.LayoutService;
import com.processmap.simulation.service.ScenarioService;
import com.processmap.symbol.service.SymbolService;
import com.processmap.telemetry.entity.TelemetryEvent;
import com.processmap.telemetry.repository.TelemetryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIOrchestratorService {

    private final AiService aiService;
    private final SymbolService symbolService;
    private final LayoutService layoutService;
    private final GraphService graphService;
    private final GraphSerializer graphSerializer;
    private final DiagramHistoryService diagramHistoryService;
    private final ScenarioService scenarioService;
    private final TelemetryEventRepository telemetryEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DiagramResponseDTO generateDiagram(GenerationRequestDTO request, UUID userId) {
        log.info("Starting orchestrated generation pipeline for user: {}", userId);

        // 1. LLM extraction
        EntityGraph entityGraph = aiService.extractEntities(request.prompt(), request.domain());

        // 2. Symbol mapping
        SymbolGraph symbolGraph = symbolService.mapSymbols(entityGraph);

        // 3. Layout engine coordinates
        LayoutGraph layoutGraph = layoutService.computeLayout(symbolGraph, request.constraints());

        // 4. Assemble DiagramGraph snapshot
        DiagramGraph diagramGraph = assembleDiagramGraph(layoutGraph);
        JsonNode graphSnapshotJson = graphSerializer.serialize(diagramGraph);

        // 5. Save Diagram revision
        DiagramResponseDTO response = diagramHistoryService.saveDiagram(request.projectId(), request.prompt(), graphSnapshotJson);

        // 6. Create default Normal Operation scenario
        scenarioService.createDefaultScenario(response.id());

        // 7. Log Telemetry Event
        TelemetryEvent telemetry = TelemetryEvent.builder()
                .eventType("generation_complete")
                .payload(objectMapper.valueToTree(Map.of("projectId", request.projectId(), "diagramId", response.id())))
                .build();
        telemetryEventRepository.save(telemetry);

        log.info("Orchestrated generation completed successfully. Diagram ID: {}", response.id());
        return response;
    }

    private DiagramGraph assembleDiagramGraph(LayoutGraph layoutGraph) {
        DiagramGraph graph = new DiagramGraph();
        graph.setDomain(layoutGraph.getDomain());

        // Map layout nodes
        layoutGraph.getNodes().forEach(ln -> {
            SymbolNode sn = ln.getNode();
            com.processmap.graph.model.TypedNode node = com.processmap.graph.model.TypedNode.builder()
                    .id(sn.id())
                    .label(sn.label())
                    .entityClass(sn.entityClass())
                    .symbolId(sn.symbolId())
                    .x(ln.getX())
                    .y(ln.getY())
                    .width(ln.getWidth())
                    .height(ln.getHeight())
                    .orientation(ln.getOrientation())
                    .locked(false)
                    .state("open")
                    .confidence(sn.confidence())
                    .medium(sn.medium())
                    .tag(sn.tag())
                    .aliases(sn.aliases())
                    .userConfirmRequired(sn.userConfirmRequired())
                    .build();
            graph.getNodes().put(node.getId(), node);
        });

        // Map layout edges
        layoutGraph.getEdges().forEach(le -> {
            EntityEdge ee = le.getEdge();
            com.processmap.graph.model.TypedEdge edge = com.processmap.graph.model.TypedEdge.builder()
                    .id(ee.id())
                    .from(ee.from())
                    .to(ee.to())
                    .medium(ee.medium())
                    .direction(ee.direction())
                    .label(ee.label())
                    .branchCondition(ee.branchCondition())
                    .routePoints(le.getRoutePoints())
                    .labelPosition(le.getLabelPosition())
                    .build();
            graph.getEdges().put(edge.getId(), edge);

            // Update adjacency list
            graph.getAdjacency().computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge.getId());
        });

        return graph;
    }
}
