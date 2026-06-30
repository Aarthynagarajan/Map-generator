# Database Dictionary: AI-Powered Technical Process Map Generator

This document provides column-level metadata for every table in the ProcessPro database schema.

---

## 1. Table: `users`
**Purpose**: Stores registered user profiles, authentication credentials, and access roles.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Unique user identity | UUID | `c56a4180-65aa-42ec-a945-5fd21dec0538` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/auth/login`, `/user/profile` | `User.id` |
| `email` | User login & communication email | VARCHAR(320) | `engineer@processpro.io` | No | None | UNIQUE, Lowercase | Yes (B-tree) | `/auth/login`, `/auth/register` | `User.email` |
| `password_hash` | BCrypt hash of user password | VARCHAR(72) | `$2a$12$e8...` | No | None | Cost 12 Hash | No | `/auth/login`, `/auth/register` | `User.passwordHash` |
| `display_name` | Public user full name | VARCHAR(100) | `Priya Sharma` | Yes | None | Max 100 chars | No | `/user/profile` | `User.displayName` |
| `role` | Authorization role | VARCHAR(20) | `'user'` | No | `'user'` | Enum: `'user'`, `'admin'` | No | `/user/profile` | `User.role` |
| `created_at` | Registration timestamp | TIMESTAMPTZ | `2026-06-29T22:00:00Z` | No | `NOW()` | None | No | `/user/profile` | `User.createdAt` |
| `updated_at` | Profile update timestamp | TIMESTAMPTZ | `2026-06-29T22:00:00Z` | No | `NOW()` | None | No | `/user/profile` | `User.updatedAt` |

---

## 2. Table: `projects`
**Purpose**: Container grouping process maps under specific engineering domains.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Unique project identity | UUID | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/projects/*` | `Project.id` |
| `user_id` | Project owner FK | UUID | `c56a4180-65aa-42ec-a945-5fd21dec0538` | No | None | FK -> `users(id)` ON DELETE CASCADE | Yes (B-tree) | `/projects/*` | `Project.user` |
| `name` | Descriptive project name | VARCHAR(255) | `Water Treatment Expansion` | No | None | Non-blank | No | `/projects/*` | `Project.name` |
| `domain` | Engineering domain category | VARCHAR(20) | `'industrial'` | No | None | Enum: `'industrial'`, `'electrical'`, `'hydraulic'` | No | `/projects/*` | `Project.domain` |
| `description` | Project scope details | TEXT | `Primary filtration SOP circuit` | Yes | None | None | No | `/projects/*` | `Project.description` |
| `created_at` | Creation timestamp | TIMESTAMPTZ | `2026-06-29T22:15:00Z` | No | `NOW()` | None | No | `/projects/*` | `Project.createdAt` |
| `updated_at` | Last modification timestamp | TIMESTAMPTZ | `2026-06-29T22:15:00Z` | No | `NOW()` | None | No | `/projects/*` | `Project.updatedAt` |

---

## 3. Table: `diagrams`
**Purpose**: Stores diagram revisions, prompt inputs, and canonical graph topologies.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Diagram version ID | UUID | `f47ac10b-58cc-4372-a567-0e02b2c3d4e5` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/generate`, `/diagrams/*` | `Diagram.id` |
| `project_id` | Parent project FK | UUID | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` | No | None | FK -> `projects(id)` ON DELETE CASCADE | Yes (B-tree) | `/diagrams/*` | `Diagram.project` |
| `version` | Sequential revision counter | INT | `1` | No | `1` | Min 1 | No | `/diagrams/*` | `Diagram.version` |
| `prompt_text` | AI input narrative | TEXT | `Water enters reservoir, pumped by P-101...` | No | None | 20-32000 chars | No | `/generate`, `/diagrams/*` | `Diagram.promptText` |
| `prompt_metadata` | AI parameters | JSONB | `{"layoutDirection":"LR","tagScheme":"ISA"}` | Yes | None | Valid JSON | No | `/diagrams/*` | `Diagram.promptMetadata` |
| `graph_snapshot` | Canonical graph topology | JSONB | `{"schemaVersion":"1.0","nodes":[],"edges":[]}` | No | None | Valid JSON | Yes (GIN) | `/generate`, `/diagrams/*` | `Diagram.graphSnapshot` |
| `thumbnail_url` | Rendered preview image | TEXT | `https://s3.aws.../thumb.png` | Yes | None | URL | No | `/projects/*` | `Diagram.thumbnailUrl` |
| `is_current` | Active version flag | BOOLEAN | `true` | No | `true` | Boolean | No | `/diagrams/*` | `Diagram.isCurrent` |
| `created_at` | Version creation timestamp | TIMESTAMPTZ | `2026-06-29T22:20:00Z` | No | `NOW()` | None | No | `/diagrams/*` | `Diagram.createdAt` |

---

## 4. Table: `scenarios`
**Purpose**: Persistent simulation interlock states and operational presets.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Scenario ID | UUID | `b9f2d1e4-3c5a-6b7c-8d9e-0f1a2b3c4d5e` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/scenarios/*` | `Scenario.id` |
| `diagram_id` | Target diagram FK | UUID | `f47ac10b-58cc-4372-a567-0e02b2c3d4e5` | No | None | FK -> `diagrams(id)` ON DELETE CASCADE | Yes (B-tree) | `/scenarios/*` | `Scenario.diagram` |
| `name` | Preset name | VARCHAR(100) | `'Normal Operation'` | No | None | Max 100 chars | No | `/scenarios/*` | `Scenario.name` |
| `stopper_states` | Active stopper positions | JSONB | `{"n3":"open","v5":"closed"}` | No | None | Valid JSON | No | `/scenarios/*` | `Scenario.stopperStates` |
| `is_default` | Default scenario flag | BOOLEAN | `true` | No | `false` | Boolean | No | `/scenarios/*` | `Scenario.isDefault` |
| `created_at` | Creation timestamp | TIMESTAMPTZ | `2026-06-29T22:20:00Z` | No | `NOW()` | None | No | `/scenarios/*` | `Scenario.createdAt` |

---

## 5. Table: `share_links`
**Purpose**: Read-only public share access links with security TTL.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Share link ID | UUID | `d3e4f5a6-b7c8-9012-3456-7890abcdef12` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/share/*` | `ShareLink.id` |
| `diagram_id` | Shared diagram FK | UUID | `f47ac10b-58cc-4372-a567-0e02b2c3d4e5` | No | None | FK -> `diagrams(id)` ON DELETE CASCADE | No | `/share/*` | `ShareLink.diagram` |
| `token` | Public access token | VARCHAR(64) | `share_9a8b7c6d5e4f3a2b1c` | No | None | UNIQUE | Yes (B-tree) | `/share/:token` | `ShareLink.token` |
| `token_hash` | BCrypt verification hash | VARCHAR(72) | `$2a$12$k9...` | No | None | Hash | No | `/share/:token` | `ShareLink.tokenHash` |
| `expires_at` | Token expiry timestamp | TIMESTAMPTZ | `2026-07-29T22:20:00Z` | No | None | Future date | No | `/share/:token` | `ShareLink.expiresAt` |
| `revoked` | Manual revocation status | BOOLEAN | `false` | No | `false` | Boolean | No | `/share/:token` | `ShareLink.revoked` |
| `created_at` | Issuance timestamp | TIMESTAMPTZ | `2026-06-29T22:20:00Z` | No | `NOW()` | None | No | `/share/*` | `ShareLink.createdAt` |

---

## 6. Table: `refresh_tokens`
**Purpose**: Hashed session refresh tokens for horizontal stateless authentication scaling.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Refresh token ID | UUID | `e5f6a7b8-c9d0-1234-5678-90abcdef1234` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/auth/refresh` | `RefreshToken.id` |
| `user_id` | Token owner FK | UUID | `c56a4180-65aa-42ec-a945-5fd21dec0538` | No | None | FK -> `users(id)` ON DELETE CASCADE | Yes (B-tree) | `/auth/refresh` | `RefreshToken.user` |
| `token_hash` | BCrypt hashed token | VARCHAR(72) | `$2a$12$x1...` | No | None | Hash | No | `/auth/refresh` | `RefreshToken.tokenHash` |
| `expires_at` | Expiry timestamp | TIMESTAMPTZ | `2026-07-06T22:00:00Z` | No | None | Future date | No | `/auth/refresh` | `RefreshToken.expiresAt` |
| `revoked` | Revocation status | BOOLEAN | `false` | No | `false` | Boolean | No | `/auth/refresh`, `/auth/logout` | `RefreshToken.revoked` |
| `created_at` | Issuance timestamp | TIMESTAMPTZ | `2026-06-29T22:00:00Z` | No | `NOW()` | None | No | `/auth/refresh` | `RefreshToken.createdAt` |

---

## 7. Table: `telemetry_events`
**Purpose**: System audit logging, performance latency tracking, and AI model refinement events.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Auto-incrementing log ID | BIGSERIAL | `10204` | No | Auto | Primary Key | Yes (PK) | Internal telemetry | `TelemetryEvent.id` |
| `user_id` | Actor user FK | UUID | `c56a4180-65aa-42ec-a945-5fd21dec0538` | Yes | None | FK -> `users(id)` ON DELETE SET NULL | No | Internal telemetry | `TelemetryEvent.userId` |
| `diagram_id` | Target diagram FK | UUID | `f47ac10b-58cc-4372-a567-0e02b2c3d4e5` | Yes | None | FK -> `diagrams(id)` ON DELETE SET NULL | No | Internal telemetry | `TelemetryEvent.diagramId` |
| `event_type` | Log classification | VARCHAR(50) | `'generation_complete'` | No | None | Max 50 chars | No | Internal telemetry | `TelemetryEvent.eventType` |
| `payload` | Metric details | JSONB | `{"latencyMs":1420,"confidence":0.96}` | Yes | None | Valid JSON | No | Internal telemetry | `TelemetryEvent.payload` |
| `created_at` | Log timestamp | TIMESTAMPTZ | `2026-06-29T22:20:00Z` | No | `NOW()` | None | Yes (B-tree) | Internal telemetry | `TelemetryEvent.createdAt` |

---

## 8. Table: `symbols`
**Purpose**: Read-only registry of all 100 MVP engineering equipment classes and standard SVG symbols.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Symbol registry ID | UUID | `71a2b3c4-d5e6-7890-1234-567890abcdef` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | `/symbols` | `Symbol.id` |
| `symbol_id` | Unique string symbol key | VARCHAR(100) | `'CENTRIFUGAL_PUMP'` | No | None | UNIQUE | No | `/symbols`, Symbol engine | `Symbol.symbolId` |
| `entity_class` | Target entity class | VARCHAR(100) | `'CENTRIFUGAL_PUMP'` | No | None | Max 100 chars | Yes (Composite) | Symbol engine | `Symbol.entityClass` |
| `domain` | Engineering domain | VARCHAR(20) | `'industrial'` | No | None | Enum: `'industrial'`, `'electrical'`, `'hydraulic'` | Yes (Composite) | `/symbols` | `Symbol.domain` |
| `svg_path` | Static vector asset URL | TEXT | `'/assets/symbols/industrial/pump.svg'` | No | None | URL | No | `/symbols`, Canvas renderer | `Symbol.svgPath` |
| `default_tag_prefix` | ISA/IEC tag prefix | VARCHAR(20) | `'P'` | Yes | None | Max 20 chars | No | Symbol engine | `Symbol.defaultTagPrefix` |
| `description` | Technical definition | TEXT | `'Standard centrifugal liquid pump'` | Yes | None | None | No | `/symbols` | `Symbol.description` |

---

## 9. Table: `synonyms`
**Purpose**: Text alias normalization table mapping user phrasing to canonical entity classes.

| Column Name | Purpose | Data Type | Example Value | Nullable | Default Value | Constraints | Indexed | Referenced by APIs | Referenced by JPA Entities |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | Synonym record ID | UUID | `82b3c4d5-e6f7-8901-2345-67890abcdef1` | No | `gen_random_uuid()` | Primary Key | Yes (PK) | Symbol engine | `Synonym.id` |
| `term` | Text alias term | VARCHAR(100) | `'isolation valve'` | No | None | UNIQUE | Yes (B-tree) | Symbol engine | `Synonym.term` |
| `entity_class` | Target entity class | VARCHAR(100) | `'GATE_VALVE'` | No | None | Max 100 chars | No | Symbol engine | `Synonym.entityClass` |
| `domain` | Engineering domain | VARCHAR(20) | `'industrial'` | No | None | Enum: `'industrial'`, `'electrical'`, `'hydraulic'` | No | Symbol engine | `Synonym.domain` |
