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
import java.util.UUID;

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
        return extractEntities(userPrompt, domain, null, null);
    }

    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    public EntityGraph extractEntities(String userPrompt, String domain, UUID userId, UUID projectId) {
        long startTimestamp = System.currentTimeMillis();
        log.info("Invoking LLM for entity extraction | User ID: {} | Project ID: {} | Domain: {}", userId, projectId, domain);

        String systemPrompt = promptBuilderService.buildSystemPrompt(domain);
        int promptLength = systemPrompt.length() + userPrompt.length();

        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(userPrompt);

        // Enforce JSON format using GoogleGenAiChatOptions (model resolved via application.yml)
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .temperature(0.1)
                .responseMimeType("application/json")
                .build();

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

        ChatResponse response;
        try {
            response = chatModel.call(prompt);
        } catch (Exception e) {
            long endTimestamp = System.currentTimeMillis();
            long latencyMs = endTimestamp - startTimestamp;
            log.error("LLM call failed | User ID: {} | Project ID: {} | Domain: {} | Latency: {}ms | Start: {} | End: {} | Error: {}",
                    userId, projectId, domain, latencyMs, startTimestamp, endTimestamp, e.getMessage(), e);
            throw new AppException(ErrorCode.LLM_TIMEOUT, "LLM call timed out or failed: " + e.getMessage(), HttpStatus.GATEWAY_TIMEOUT);
        }

        long endTimestamp = System.currentTimeMillis();
        long latencyMs = endTimestamp - startTimestamp;
        String rawContent = response.getResult().getOutput().getText();
        String cleanedContent = cleanJsonContent(rawContent);

        if (log.isDebugEnabled()) {
            log.debug("Raw Gemini response received: {}", rawContent);
        }

        String modelUsed = response.getMetadata() != null ? response.getMetadata().getModel() : "unknown";
        int responseLength = cleanedContent != null ? cleanedContent.length() : 0;

        log.info("LLM call successful | User ID: {} | Project ID: {} | Domain: {} | Model: {} | Latency: {}ms | Start: {} | End: {} | Prompt Length: {} | Response Length: {}",
                userId, projectId, domain, modelUsed, latencyMs, startTimestamp, endTimestamp, promptLength, responseLength);

        try {
            EntityGraph entityGraph = objectMapper.readValue(cleanedContent, EntityGraph.class);
            return entityGraph;
        } catch (Exception e) {
            log.error("Failed to parse LLM structured response to EntityGraph | User ID: {} | Project ID: {}", userId, projectId, e);
            if (log.isDebugEnabled()) {
                log.debug("Malformed JSON content: {}", cleanedContent);
            }
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Failed to parse structured JSON from LLM: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String cleanJsonContent(String content) {
        if (content == null) {
            return "";
        }
        String cleaned = content.trim();

        // Strip markdown code block markers
        if (cleaned.startsWith("```")) {
            int firstLineEnd = cleaned.indexOf('\n');
            if (firstLineEnd != -1) {
                cleaned = cleaned.substring(firstLineEnd).trim();
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }

        // Find first '{' and last '}'
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }
}
