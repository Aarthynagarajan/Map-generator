package com.processmap.project.service;

import com.processmap.dto.ProjectRequestDTO;
import com.processmap.dto.ProjectResponseDTO;
import com.processmap.exception.AppException;
import com.processmap.project.entity.Domain;
import com.processmap.project.entity.Project;
import com.processmap.project.mapper.ProjectMapper;
import com.processmap.project.repository.ProjectRepository;
import com.processmap.user.entity.User;
import com.processmap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User user;
    private UUID userId;
    private Project project;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        user = User.builder().id(userId).email("test@processpro.io").build();
        project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .domain(Domain.INDUSTRIAL)
                .user(user)
                .build();
    }

    @Test
    void createProject_success() {
        ProjectRequestDTO request = new ProjectRequestDTO("Test Project", "industrial", "Description");
        ProjectResponseDTO responseDTO = new ProjectResponseDTO(projectId, userId, "Test Project", "INDUSTRIAL", "Description", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponseDTO(project)).thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.createProject(request, userId);

        assertNotNull(result);
        assertEquals("Test Project", result.name());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getProject_success() {
        ProjectResponseDTO responseDTO = new ProjectResponseDTO(projectId, userId, "Test Project", "INDUSTRIAL", "Description", null, null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.getProject(projectId, userId);

        assertNotNull(result);
        assertEquals(projectId, result.id());
    }

    @Test
    void getProject_accessDenied_throwsException() {
        UUID otherUserId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(AppException.class, () -> projectService.getProject(projectId, otherUserId));
    }
}
