package com.processmap.simulation.controller;

import com.processmap.common.ApiResponse;
import com.processmap.dto.ScenarioRequestDTO;
import com.processmap.dto.ScenarioResponseDTO;
import com.processmap.simulation.service.ScenarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping("/api/v1/diagrams/{id}/scenarios")
    public ApiResponse<List<ScenarioResponseDTO>> listScenarios(@PathVariable UUID id) {
        return ApiResponse.of(scenarioService.listScenarios(id));
    }

    @PostMapping("/api/v1/diagrams/{id}/scenarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScenarioResponseDTO> createScenario(
            @PathVariable UUID id,
            @Valid @RequestBody ScenarioRequestDTO request) {
        // Enforce matching diagram ID path parameter
        ScenarioRequestDTO enriched = new ScenarioRequestDTO(id, request.name(), request.stopperStates(), request.isDefault());
        return ApiResponse.of(scenarioService.createScenario(enriched));
    }

    @PatchMapping("/api/v1/scenarios/{id}")
    public ApiResponse<ScenarioResponseDTO> updateScenario(
            @PathVariable UUID id,
            @Valid @RequestBody ScenarioRequestDTO request) {
        return ApiResponse.of(scenarioService.updateScenario(id, request));
    }

    @DeleteMapping("/api/v1/scenarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScenario(@PathVariable UUID id) {
        scenarioService.deleteScenario(id);
    }
}
