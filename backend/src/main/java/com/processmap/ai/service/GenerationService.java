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
    public void generateAsync(GenerationRequestDTO request, UUID userId, SseEmitter emitter) {
        log.info("Starting async generation task for user: {}", userId);
        try {
            // Stage 1: Parsing
            sendProgress(emitter, "parsing", 20);

            // Simulate parsing stage delay or call AI orchestrator components sequentially
            // Stage 2: Symbol Mapping
            sendProgress(emitter, "symbol_mapping", 50);

            // Stage 3: Layout computation
            sendProgress(emitter, "layout", 80);

            // Execute full pipeline and persistence
            DiagramResponseDTO response = aiOrchestratorService.generateDiagram(request, userId);

            // Send completion event
            SseEmitter.SseEventBuilder completeEvent = SseEmitter.event()
                    .name("complete")
                    .data(Map.of("diagramId", response.id().toString(), "payload", response));
            emitter.send(completeEvent);
            emitter.complete();
            log.info("Async generation task completed successfully for diagram ID: {}", response.id());

        } catch (Exception ex) {
            log.error("Async generation task failed", ex);
            try {
                SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                        .name("error")
                        .data(Map.of("code", "GENERATION_FAILED", "message", ex.getMessage()));
                emitter.send(errorEvent);
                emitter.completeWithError(ex);
            } catch (IOException ioException) {
                log.error("Failed to send error event to client", ioException);
            }
        }
    }

    private void sendProgress(SseEmitter emitter, String stage, int pct) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("progress")
                    .data(Map.of("stage", stage, "pct", pct));
            emitter.send(event);
        } catch (IOException e) {
            log.warn("Failed to send progress event for stage {}: {}", stage, e.getMessage());
        }
    }
}
