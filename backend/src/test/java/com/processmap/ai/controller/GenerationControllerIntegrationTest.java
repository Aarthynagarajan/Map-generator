package com.processmap.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.dto.GenerationConstraints;
import com.processmap.dto.GenerationRequestDTO;
import com.processmap.ai.service.GenerationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GenerationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GenerationService generationService;

    @Test
    void generate_unauthorized_returns401() throws Exception {
        GenerationRequestDTO request = new GenerationRequestDTO(
                UUID.randomUUID(),
                "Water enters reservoir, pumped by P-101 to T-101",
                "industrial",
                new GenerationConstraints("LR", "ISA", "medium")
        );

        mockMvc.perform(post("/api/v1/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generate_authorized_success() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "USER", "user@processpro.io");

        GenerationRequestDTO request = new GenerationRequestDTO(
                UUID.randomUUID(),
                "Water enters reservoir, pumped by P-101 to T-101",
                "industrial",
                new GenerationConstraints("LR", "ISA", "medium")
        );

        doNothing().when(generationService).generateAsync(any(GenerationRequestDTO.class), eq(userId), any());

        mockMvc.perform(post("/api/v1/generate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
