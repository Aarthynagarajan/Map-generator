package com.processmap.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SafeSseEmitter {
    private final SseEmitter emitter;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public SafeSseEmitter(SseEmitter emitter) {
        this.emitter = emitter;
    }

    public boolean isClosed() {
        return completed.get();
    }

    public synchronized void send(SseEmitter.SseEventBuilder eventBuilder) throws IOException {
        if (completed.get()) {
            log.warn("Attempted to send event after emitter completion/closure. Skipping.");
            return;
        }
        try {
            emitter.send(eventBuilder);
        } catch (IOException e) {
            markClosed();
            throw e;
        }
    }

    public synchronized void complete() {
        if (completed.compareAndSet(false, true)) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Failed to complete emitter: {}", e.getMessage());
            }
        }
    }

    public synchronized void completeWithError(Throwable t) {
        if (completed.compareAndSet(false, true)) {
            try {
                emitter.completeWithError(t);
            } catch (Exception e) {
                log.warn("Failed to complete emitter with error: {}", e.getMessage());
            }
        }
    }

    public void markClosed() {
        completed.set(true);
    }
}
