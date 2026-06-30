# Root Cause Analysis — SSE Pipeline Stabilization

This document outlines the root cause analysis, modifications made, and verification results for the Server-Sent Events (SSE) pipeline stabilization.

---

## 1. Root Cause Analysis

### Issue A: `AuthorizationDeniedException` during Async Execution
*   **Root Cause**: In Spring Security 6 / Spring Boot 3, authentication filters are skipped on `ASYNC` servlet request dispatches by default (as `shouldNotFilterAsyncDispatch()` in `OncePerRequestFilter` defaults to `true`). 
    Because our stateless API relies on JWT tokens (which do not persist in session storage), the `SecurityContext` on Tomcat's async execution dispatch thread was empty. When Spring Security's authorization filters ran on the async dispatch, they failed to authenticate the request, throwing an `AuthorizationDeniedException` and prematurely closing the SSE connection.
*   **Solution**: Overrode `shouldNotFilterAsyncDispatch()` to return `false` in `JwtAuthenticationFilter.java`. This guarantees that Bearer token validation and context mapping execute on Tomcat's async dispatches.

### Issue B: Incomplete chunked encoding (`ERR_INCOMPLETE_CHUNKED_ENCODING`)
*   **Root Cause**: When the generation process completed, the server closed the connection via `emitter.complete()`. However, the frontend reader loop in `Workspace.tsx` continued to block wait for stream chunks on `reader.read()`. The abrupt server closure while the client reader was in an active read state caused the browser to raise chunked encoding error warnings.
*   **Solution**: Updated the reader loop in `Workspace.tsx` to immediately cancel the stream reader (`reader.cancel()`), release its lock (`reader.releaseLock()`), and abort the fetch request using `AbortController` as soon as the `complete` or `error` event is parsed.

---

## 2. Exact Files Modified & Code Changes

### [JwtAuthenticationFilter.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/security/JwtAuthenticationFilter.java)
Added the `shouldNotFilterAsyncDispatch` override:
```java
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }
```

### [GenerationService.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/ai/service/GenerationService.java)
Updated progress sending to check for client disconnects and abort background processing:
```java
    private void sendProgress(SseEmitter emitter, String stage, int pct) {
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .name("progress")
                    .data(Map.of("stage", stage, "pct", pct));
            emitter.send(event);
        } catch (IOException e) {
            log.warn("Failed to send progress event for stage {}: {}. Aborting task.", stage, e.getMessage());
            throw new RuntimeException("Client disconnected: " + e.getMessage(), e);
        }
    }
```
Added catch blocks to gracefully handle `IOException` on completion:
```java
        } catch (Exception ex) {
            log.error("Async generation task failed", ex);
            try {
                SseEmitter.SseEventBuilder errorEvent = SseEmitter.event()
                        .name("error")
                        .data(Map.of("code", "GENERATION_FAILED", "message", ex.getMessage()));
                emitter.send(errorEvent);
                emitter.completeWithError(ex);
            } catch (Exception ioException) {
                log.warn("Could not write error event to client (likely client disconnected): {}", ioException.getMessage());
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        }
```

### [Workspace.tsx](file:///d:/New%20folder%20(4)/processpro/frontend/src/pages/Workspace.tsx)
Wired an `AbortController` to track the connection, abort on unmount or cancellation, and clean up locks:
```typescript
    } finally {
      if (reader) {
        try {
          reader.cancel();
          reader.releaseLock();
        } catch (e) {
          console.warn('Error closing stream reader:', e);
        }
      }
      controller.abort();
      if (abortControllerRef.current === controller) {
        abortControllerRef.current = null;
      }
    }
```

---

## 3. Verification Performed

- **Backend Maven Tests**:
  - Command: `mvn test`
  - Result: ✅ **BUILD SUCCESS** (25/25 tests passed cleanly).
- **TypeScript Typecheck**:
  - Command: `npm run typecheck`
  - Result: ✅ **SUCCESS** (0 compile warnings or errors).
- **Production Bundler**:
  - Command: `npm run build`
  - Result: ✅ **SUCCESS** (bundled assets in 3.32s).
