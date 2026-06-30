# Debug Report

This report documents the bugs resolved during the project stabilization phase.

---

## 1. Timeline Version Restoration Gap
*   **Bug Found**: The frontend version history UI (timeline) attempts to switch historical graph snapshots using the endpoint:
    `POST /api/v1/diagrams/{id}/history/restore?version={version}`
    However, the endpoint was missing on the backend controller, resulting in `404 Not Found` errors.
*   **Root Cause**: Lack of corresponding REST mapping implementation on the backend controller to update project-to-diagram version references.
*   **Files Modified**:
    - [DiagramController.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/history/controller/DiagramController.java)
    - [DiagramHistoryService.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/history/service/DiagramHistoryService.java)
*   **Exact Code Changes**:
    Exposed POST endpoint in `DiagramController`:
    ```java
    @PostMapping("/{id}/history/restore")
    public ApiResponse<DiagramResponseDTO> restoreVersion(
            @PathVariable UUID id,
            @RequestParam int version,
            @AuthenticationPrincipal UUID userId) {
        return ApiResponse.of(diagramHistoryService.restoreVersion(id, version, userId));
    }
    ```
    Implemented version swap toggling on `DiagramHistoryService.restoreVersion` setting the targeted version's `isCurrent` property to `true` and others to `false`.
*   **Verification**: All surefire tests passed. Timeline restoration works end-to-end.

---

## 2. Dynamic Port Mapping Failures
*   **Bug Found**: If Vite ran on a dynamic port (e.g. 5174) due to 5173 being in use, the frontend failed to connect to the backend because of a hardcoded port check.
*   **Root Cause**: Hardcoded check `window.location.port === '5173'` resolved the target base URL incorrect.
*   **Files Modified**:
    - [apiClient.ts](file:///d:/New%20folder%20(4)/processpro/frontend/src/services/apiClient.ts)
    - [Workspace.tsx](file:///d:/New%20folder%20(4)/processpro/frontend/src/pages/Workspace.tsx)
*   **Exact Code Changes**:
    ```typescript
    const getBaseUrl = () => {
      if (typeof window !== 'undefined') {
        return window.location.hostname === 'localhost' && window.location.port !== '8080'
          ? 'http://localhost:8080'
          : window.location.origin;
      }
      return 'http://localhost:8080';
    };
    ```
*   **Verification**: Verified successful routing on arbitrary localhost ports.

---

## 3. SSE Generation Stream Lifecycle Errors
*   **Bug Found**: Client socket connections were sometimes closed prematurely or resource leakage happened during background generation.
*   **Root Cause**: `SseEmitter` was initialized without timeout or error listener callbacks.
*   **File Modified**: [GenerationController.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/ai/controller/GenerationController.java)
*   **Exact Code Changes**:
    ```java
    emitter.onCompletion(() -> log.debug("Emitter completed for user {}", userId));
    emitter.onTimeout(() -> {
        log.warn("Emitter timed out for user {}", userId);
        emitter.complete();
    });
    emitter.onError((ex) -> {
        log.error("Emitter error for user {}", userId, ex);
        emitter.complete();
    });
    ```
*   **Verification**: Emitters release correctly on connection drops.

---

## 4. History API Contract Sync
*   **Bug Found**: The history retrieval called `/api/v1/diagrams/${id}/history`, resulting in `404 Not Found`.
*   **Root Cause**: The backend listing endpoint expects project UUID on path `/api/v1/diagrams/project/{projectId}`.
*   **File Modified**: [diagramService.ts](file:///d:/New%20folder%20(4)/processpro/frontend/src/services/diagramService.ts)
*   **Exact Code Changes**:
    ```diff
    -  getHistory: async (id: string) => {
    -    const response = await apiClient.get<any>(`/api/v1/diagrams/${id}/history`);
    +  getHistory: async (projectId: string) => {
    +    const response = await apiClient.get<any>(`/api/v1/diagrams/project/${projectId}`);
    ```
*   **Verification**: Clean synchronization, diagram timelines fetch successfully.

---

## 5. Security Access Denied Context Leakage
*   **Bug Found**: 403 Forbidden events leaked Java exception details.
*   **Root Cause**: Access denied events were not handled explicitly inside Security Filter chains.
*   **File Modified**: [SecurityConfig.java](file:///d:/New%20folder%20(4)/processpro/backend/src/main/java/com/processmap/config/SecurityConfig.java)
*   **Exact Code Changes**:
    Wired custom `AccessDeniedHandler` payload writer.
*   **Verification**: Custom clean error response returned on access violations.

---

## 6. Remaining Risks
*   **None**: Zero compile errors or warnings; all unit and integration tests passed.
