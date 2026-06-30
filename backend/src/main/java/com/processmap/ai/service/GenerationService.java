package com.processmap.ai.service;

import com.processmap.dto.DiagramResponseDTO;
import com.processmap.dto.GenerationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationService {

    private final AIOrchestratorService aiOrchestratorService;

    @Async("generationTaskExecutor")
    public void generateAsync(GenerationRequestDTO request, UUID userId, SafeSseEmitter emitter) {
        log.info("Starting async generation task for user: {}", userId);
        try {
            // Execute full pipeline and persistence passing progress callback
            DiagramResponseDTO response = aiOrchestratorService.generateDiagram(request, userId, (stage, pct) -> {
                sendProgress(emitter, stage, pct);
            });

            // Send completion event if not already closed
            if (!emitter.isClosed()) {
                SseEmitter.SseEventBuilder completeEvent = SseEmitter.event()
                        .name("complete")
                        .data(Map.of("diagramId", response.id().toString(), "payload", response));
                emitter.send(completeEvent);
                emitter.complete();
                log.info("Async generation task completed successfully for diagram ID: {}", response.id());
            }

        } catch (Exception ex) {
            log.error("Async generation task failed", ex);
            if (!emitter.isClosed()) {
                try {
                    SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", "GENERATION_FAILED", "message", ex.getMessage()));
                    emitter.send(errorEvent);
                } catch (Exception ioException) {
                    log.warn("Could not write error event to client: {}", ioException.getMessage());
                }
                emitter.completeWithError(ex);
            }
        }
    }

    private void sendProgress(SafeSseEmitter emitter, String stage, int pct) {
        if (emitter.isClosed()) {
            throw new RuntimeException("Client disconnected or emitter closed");
        }
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("progress")
                    .data(Map.of("stage", stage, "pct", pct));
            emitter.send(event);
        } catch (IOException e) {
            log.warn("Failed to send progress event for stage {}: {}. Aborting task.", stage, e.getMessage());
            emitter.markClosed();
            throw new RuntimeException("Client disconnected: " + e.getMessage(), e);
        }
    }
}
