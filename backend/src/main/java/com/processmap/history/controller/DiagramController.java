package com.processmap.history.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.processmap.common.ApiResponse;
import com.processmap.dto.DiagramResponseDTO;
import com.processmap.history.service.DiagramHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagrams")
@RequiredArgsConstructor
public class DiagramController {

    private final DiagramHistoryService diagramHistoryService;

    @GetMapping("/{id}")
    public ApiResponse<DiagramResponseDTO> getDiagram(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(diagramHistoryService.getDiagram(id, userId));
    }

    @PatchMapping("/{id}")
    public ApiResponse<DiagramResponseDTO> updateDiagram(
            @PathVariable UUID id,
            @RequestBody JsonNode partialUpdate,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(diagramHistoryService.updateDiagram(id, userId, partialUpdate));
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<DiagramResponseDTO>> listVersions(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(diagramHistoryService.listVersions(projectId, userId));
    }

    @PostMapping("/{id}/history/restore")
    public ApiResponse<DiagramResponseDTO> restoreVersion(
            @PathVariable UUID id,
            @RequestParam int version,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(diagramHistoryService.restoreVersion(id, version, userId));
    }
}
