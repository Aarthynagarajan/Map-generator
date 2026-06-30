package com.processmap.project.controller;

import com.processmap.common.ApiResponse;
import com.processmap.dto.ProjectRequestDTO;
import com.processmap.dto.ProjectResponseDTO;
import com.processmap.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(projectService.createProject(request, userId));
    }

    @GetMapping
    public ApiResponse<Page<ProjectResponseDTO>> listProjects(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.of(projectService.listProjects(userId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectResponseDTO> getProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(projectService.getProject(id, userId));
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProjectResponseDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(projectService.updateProject(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        projectService.deleteProject(id, userId);
    }
}
