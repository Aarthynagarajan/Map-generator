package com.processmap.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.dto.ProjectRequestDTO;
import com.processmap.dto.ProjectResponseDTO;
import com.processmap.project.service.ProjectService;
import com.processmap.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createProject_unauthorized_returns401() throws Exception {
        ProjectRequestDTO request = new ProjectRequestDTO("New Project", "industrial", "Desc");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProject_authorized_success() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "USER", "user@processpro.io");

        ProjectRequestDTO request = new ProjectRequestDTO("New Project", "industrial", "Desc");
        ProjectResponseDTO response = new ProjectResponseDTO(UUID.randomUUID(), userId, "New Project", "INDUSTRIAL", "Desc", null, null);

        when(projectService.createProject(any(ProjectRequestDTO.class), any(UUID.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Project"));
    }
}
