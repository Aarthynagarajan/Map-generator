package com.processmap.project.service;

import com.processmap.dto.ProjectRequestDTO;
import com.processmap.dto.ProjectResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import com.processmap.project.entity.Domain;
import com.processmap.project.entity.Project;
import com.processmap.project.mapper.ProjectMapper;
import com.processmap.project.repository.ProjectRepository;
import com.processmap.user.entity.User;
import com.processmap.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found", HttpStatus.NOT_FOUND));

        Domain domain = Domain.fromString(request.domain());
        if (domain == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Invalid domain value", HttpStatus.BAD_REQUEST);
        }

        Project project = Project.builder()
                .user(user)
                .name(request.name())
                .domain(domain)
                .description(request.description())
                .build();

        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found", HttpStatus.NOT_FOUND));

        validateOwnership(project, userId);
        return projectMapper.toResponseDTO(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> listProjects(UUID userId, Pageable pageable) {
        Page<Project> projects = projectRepository.findByUserId(userId, pageable);
        return projects.map(projectMapper::toResponseDTO);
    }

    @Transactional
    public ProjectResponseDTO updateProject(UUID projectId, ProjectRequestDTO request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found", HttpStatus.NOT_FOUND));

        validateOwnership(project, userId);

        Domain domain = Domain.fromString(request.domain());
        if (domain == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Invalid domain value", HttpStatus.BAD_REQUEST);
        }

        project.setName(request.name());
        project.setDomain(domain);
        project.setDescription(request.description());

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found", HttpStatus.NOT_FOUND));

        validateOwnership(project, userId);
        projectRepository.delete(project);
    }

    private void validateOwnership(Project project, UUID userId) {
        if (!project.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Access denied. You do not own this project", HttpStatus.FORBIDDEN);
        }
    }
}
