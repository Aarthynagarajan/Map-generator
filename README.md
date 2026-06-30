# ProcessPro — AI-Powered Technical Process Map Generator

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Backend](https://img.shields.io/badge/Spring_Boot-3.4.1-brightgreen.svg)]()
[![Frontend](https://img.shields.io/badge/React-19-blue.svg)]()
[![Database](https://img.shields.io/badge/PostgreSQL-16-blue.svg)]()

ProcessPro is an enterprise-grade web application that converts natural language process descriptions (P&IDs, electrical single-line diagrams, hydraulic loops) into interactive React Flow schemas. It leverages Spring AI, structured layouts, level-based BFS coordinate mapping, and full stopper valve/circuit breaker flow simulations.

---

## Key Features

- **AI-Powered Graph Extraction**: Seamlessly parses text prompts into structured canonical schemas.
- **Seeded Symbol Resolution**: Resolves components into exact, synonym-mapped, or fuzzy standard symbols (P&ID ISA / IEC standards).
- **BFS Layout Engine**: Distributes components vertically and horizontally to prevent node overlaps.
- **Dynamic Flow Simulation**: Traces live material/current flow using local BFS calculations. Interactive stoppers (valves/breakers) block or enable flow dynamically.
- **Version Control & Revisions**: Save and merge layout snapshots, track history, and restore previous versions.
- **Export Capabilities**: Download process schemas in standard SVG, PNG, or JSON format.
- **Demo Mode**: Ships pre-loaded with three reference projects:
  - Industrial Cooling Water System
  - Electrical Power Distribution
  - Hydraulic Press System

---

## Technology Stack

### Backend Monolith
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.x (Web, JPA, Security, Actuator, Spring AI)
- **Database**: PostgreSQL 16 (using GIN indexes on JSONB fields)
- **Migrations**: Flyway

### Frontend Client
- **Framework**: React 19 + TypeScript + Vite
- **Canvas Engine**: `@xyflow/react` (React Flow)
- **State Management**: Zustand
- **Query Cache**: React Query v5
- **HTTP Client**: Axios (with custom JWT rotation interceptor)
- **Styling**: Tailwind CSS

---

## Setup & Execution

For quick local setup using Docker Compose:

```bash
docker compose up --build
```

The application will be accessible at:
- **Web UI**: `http://localhost`
- **Backend API Documentation**: `http://localhost:8080/swagger-ui/index.html`

Please read [INSTALL.md](file:///d:/New%20folder%20(4)/processpro/INSTALL.md) and [DEPLOYMENT.md](file:///d:/New%20folder%20(4)/processpro/DEPLOYMENT.md) for more details.
