# Project Creation Contract Synchronization Report — ProcessPro

This report documents the contract synchronization performed between the frontend project creation modal payload and the backend `ProjectRequestDTO` validation parameters.

---

## 1. Backend DTO Specifications (`ProjectRequestDTO.java`)

```java
public record ProjectRequestDTO(
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name cannot exceed 255 characters")
    String name,

    @NotBlank(message = "Domain is required")
    String domain,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description
) {}
```

---

## 2. Frontend Project Payload (`Dashboard.tsx`)

### Before Fix:
```json
{
  "name": "...",
  "description": "..."
}
```

### After Fix:
```json
{
  "name": "...",
  "description": "...",
  "domain": "INDUSTRIAL" // Or ELECTRICAL, HYDRAULIC
}
```

---

## 3. Differences Found & Mismatches
1. **Missing Domain Parameter**: The backend has `@NotBlank` validation for `domain`, which was completely omitted from the frontend Create Project modal. All project creations failed with a validation error: `ProjectRequestDTO.domain = null` (400 Bad Request).
2. **Missing Update Parameter**: The update project endpoint on the backend also uses the same `ProjectRequestDTO`, meaning the existing domain must be passed when renaming a project.

---

## 4. Fixes Applied & Sync Steps
- **Model Interface Sync**: Updated `Project` interface inside [types/index.ts](file:///d:/New%20folder%20(4)/processpro/frontend/src/types/index.ts) to define the `domain` parameter.
- **State parameters**: Added `newProjectDomain` and `renameDomain` states inside [Dashboard.tsx](file:///d:/New%20folder%20(4)/processpro/frontend/src/pages/Dashboard.tsx).
- **Form controls**: Integrated a dropdown selector for the domain field (INDUSTRIAL selected by default) inside the creation modal.
- **Service & Hook payload**: Updated `projectService.createProject` and `projectService.updateProject` to accept and transmit the `domain` parameter.
- **Dashboard badge**: Displayed the domain tag in uppercase text as a badge on each card in the dashboard grid.

---

## 5. Verification Performed
- **TypeScript Typechecking**: Verified type safety using `npm run typecheck` (0 errors).
- **Vite production compilation**: Built the client successfully in 3.04s.
- **Backend Test Suite**: Ran `mvn clean test` successfully (**BUILD SUCCESS**).
- **E2E flow check**: Created projects with distinct domains, verified their badges render on cards, and confirmed workspace redirects successfully.
