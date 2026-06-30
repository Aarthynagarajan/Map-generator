package com.processmap.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.ai.model.EntityGraph;
import com.processmap.ai.model.EntityNode;
import com.processmap.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private PromptBuilderService promptBuilderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(chatModel, promptBuilderService, objectMapper);
    }

    @Test
    void extractEntities_success() throws Exception {
        String mockResponseContent = """
        {
          "nodes": [
            {
              "id": "n1",
              "label": "Water Reservoir",
              "entityClass": "RESERVOIR",
              "confidence": 0.98,
              "aliases": [],
              "medium": "liquid",
              "userConfirmRequired": false
            }
          ],
          "edges": [],
          "branches": [],
          "domain": "industrial"
        }
        """;

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = new org.springframework.ai.chat.messages.AssistantMessage(mockResponseContent);

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(assistantMessage);
        when(promptBuilderService.buildSystemPrompt("industrial")).thenReturn("System Prompt Mock");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        EntityGraph result = aiService.extractEntities("Water reservoir flows...", "industrial");

        assertNotNull(result);
        assertEquals("industrial", result.domain());
        assertEquals(1, result.nodes().size());
        assertEquals("RESERVOIR", result.nodes().get(0).entityClass());
    }

    @Test
    void extractEntities_malformedJson_throwsException() {
        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = new org.springframework.ai.chat.messages.AssistantMessage("{ malformed json }");

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(assistantMessage);
        when(promptBuilderService.buildSystemPrompt("industrial")).thenReturn("System Prompt Mock");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        assertThrows(AppException.class, () -> aiService.extractEntities("test", "industrial"));
    }

    @Test
    void extractEntities_timeout_throwsException() {
        when(promptBuilderService.buildSystemPrompt("industrial")).thenReturn("System Prompt Mock");
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("OpenAI Timeout"));

        assertThrows(AppException.class, () -> aiService.extractEntities("test", "industrial"));
    }
}
