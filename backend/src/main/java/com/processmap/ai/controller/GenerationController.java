package com.processmap.ai.controller;

import com.processmap.dto.GenerationRequestDTO;
import com.processmap.ai.service.GenerationService;
import com.processmap.ai.service.SafeSseEmitter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GenerationController {

    private final GenerationService generationService;
    private final Validator validator;

    @PostMapping(value = "/api/v1/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(
            @RequestBody GenerationRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        
        // 5-minute timeout matching spring.mvc.async.request-timeout
        SseEmitter emitter = new SseEmitter(300000L);
        SafeSseEmitter safeEmitter = new SafeSseEmitter(emitter);

        emitter.onCompletion(() -> {
            log.debug("Emitter completed for user {}", userId);
            safeEmitter.markClosed();
        });
        emitter.onTimeout(() -> {
            log.warn("Emitter timed out for user {}", userId);
            safeEmitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("Emitter error for user {}", userId, ex);
            safeEmitter.complete();
        });

        // Manual validation to prevent validation exceptions escaping to GlobalExceptionHandler
        Set<ConstraintViolation<GenerationRequestDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            try {
                SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                        .name("error")
                        .data(Map.of("code", "VALIDATION_FAILED", "message", message));
                safeEmitter.send(errorEvent);
                safeEmitter.complete();
            } catch (Exception e) {
                log.error("Failed to send validation error to emitter", e);
            }
            return emitter;
        }

        try {
            log.info("Authenticated user = {}", userId);
            generationService.generateAsync(request, userId, safeEmitter);
        } catch (Exception ex) {
            log.error("Failed to start generation async task for user {}", userId, ex);
            try {
                SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                        .name("error")
                        .data(Map.of("code", "GENERATION_FAILED", "message", ex.getMessage()));
                safeEmitter.send(errorEvent);
                safeEmitter.complete();
            } catch (Exception e) {
                log.error("Failed to send error event to emitter", e);
            }
        }
        return emitter;
    }
}
