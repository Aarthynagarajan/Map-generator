package com.processmap.ai.controller;

import com.processmap.dto.GenerationRequestDTO;
import com.processmap.ai.service.GenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping(value = "/api/v1/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(
            @Valid @RequestBody GenerationRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        // Emitter timeout: 45 seconds (exceeding standard 35s read timeout)
        SseEmitter emitter = new SseEmitter(45000L);

        emitter.onCompletion(() -> log.debug("Emitter completed for user {}", userId));
        emitter.onTimeout(() -> {
            log.warn("Emitter timed out for user {}", userId);
            emitter.complete();
        });
        emitter.onError((ex) -> {
            log.error("Emitter error for user {}", userId, ex);
            emitter.complete();
        });
        log.info("Authenticated user = {}", userId);
        generationService.generateAsync(request, userId, emitter);
        return emitter;
    }
}
