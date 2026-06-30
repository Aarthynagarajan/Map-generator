# Testing & Validation Guide

ProcessPro relies on comprehensive automated testing for both backend and frontend layers.

## Backend Tests

We utilize JUnit 5, Mockito, and Spring Boot Test:
- **Unit Tests**: Coverage of layout calculations, cycle detections, symbol synaptic mapping, and token providers.
- **Integration Tests**: Tests matching REST endpoints under Auth, Projects, and Diagram History (with MockMvc).

Run all backend tests:
```bash
cd backend
mvn clean test
```

---

## Frontend Typechecking

```bash
cd frontend
npm run typecheck
```
This performs a compile check verifying zero warnings or implicit-any compile faults.
