package com.processmap.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmap.ai.model.EntityGraph;
import com.processmap.exception.AppException;
import com.processmap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ChatModel chatModel;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper objectMapper;

    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    public EntityGraph extractEntities(String userPrompt, String domain) {
        log.info("Invoking LLM for entity extraction in domain: {}", domain);
        long startTime = System.currentTimeMillis();

        String systemPrompt = promptBuilderService.buildSystemPrompt(domain);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(userPrompt);

        // Enforce JSON format using GoogleGenAiChatOptions
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model("gemini-1.5-flash")
                .temperature(0.1)
                .responseMimeType("application/json")
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        ChatResponse response;
        try {
            response = chatModel.call(prompt);
        } catch (Exception e) {
            log.error("LLM call failed: {}", e.getMessage());
            throw new AppException(ErrorCode.LLM_TIMEOUT, "LLM call timed out or failed: " + e.getMessage(), HttpStatus.GATEWAY_TIMEOUT);
        }

        long latencyMs = System.currentTimeMillis() - startTime;
        String content = response.getResult().getOutput().getText();

        log.info("LLM call completed in {}ms", latencyMs);

        try {
            EntityGraph entityGraph = objectMapper.readValue(content, EntityGraph.class);
            return entityGraph;
        } catch (Exception e) {
            log.error("Failed to parse LLM structured response to EntityGraph", e);
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Failed to parse structured JSON from LLM: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
