# API Reference Documentation

All endpoints require a `Bearer JWT` token in the `Authorization` header, except those under `/auth/*` and public share links.

---

## Authentication Endpoints

### User Registration
- **URL**: `POST /auth/register`
- **Request Body**: `{"email": "...", "password": "..."}`
- **Response**: `ApiResponse<RegisterResponseDTO>`

### User Login
- **URL**: `POST /auth/login`
- **Request Body**: `{"email": "...", "password": "..."}`
- **Response**: `ApiResponse<LoginResponseDTO>`

---

## Project Endpoints

### Create Project
- **URL**: `POST /api/v1/projects`
- **Request Body**: `{"name": "Project Name", "description": "Details"}`
- **Response**: `ApiResponse<ProjectResponseDTO>`

### List Projects (Paginated)
- **URL**: `GET /api/v1/projects?page=0&size=10&search=valves&sortBy=updatedAt`
- **Response**: `ApiResponse<Page<ProjectResponseDTO>>`

---

## Generation Endpoints

### Async Stream Generation (SSE)
- **URL**: `POST /api/v1/generate`
- **Request Body**:
  ```json
  {
    "projectId": "uuid",
    "prompt": "Water flows from P-101 to T-101",
    "domain": "industrial",
    "constraints": {
      "direction": "LR",
      "symbolStandard": "ISA",
      "spacingDensity": "medium"
    }
  }
  ```
- **Response**: Event stream of progress (`parsing`, `symbol_mapping`, `layout`, `complete`, `error`).
