-- V2__constraints_and_indexes.sql: Unique constraints, Foreign Keys, B-Tree and GIN indexes

-- Unique Constraints
ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email);
ALTER TABLE share_links ADD CONSTRAINT uq_share_links_token UNIQUE (token);

-- Foreign Key Constraints with Cascade Rules
ALTER TABLE projects 
    ADD CONSTRAINT fk_projects_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE diagrams 
    ADD CONSTRAINT fk_diagrams_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

ALTER TABLE scenarios 
    ADD CONSTRAINT fk_scenarios_diagram 
    FOREIGN KEY (diagram_id) REFERENCES diagrams(id) ON DELETE CASCADE;

ALTER TABLE share_links 
    ADD CONSTRAINT fk_share_links_diagram 
    FOREIGN KEY (diagram_id) REFERENCES diagrams(id) ON DELETE CASCADE;

ALTER TABLE refresh_tokens 
    ADD CONSTRAINT fk_refresh_tokens_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE telemetry_events 
    ADD CONSTRAINT fk_telemetry_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE telemetry_events 
    ADD CONSTRAINT fk_telemetry_diagram 
    FOREIGN KEY (diagram_id) REFERENCES diagrams(id) ON DELETE SET NULL;

-- B-Tree Indexes for Performance Optimization
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_diagrams_project_id ON diagrams(project_id);
CREATE INDEX idx_scenarios_diagram_id ON scenarios(diagram_id);
CREATE INDEX idx_share_links_token ON share_links(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_telemetry_created ON telemetry_events(created_at);

-- GIN Index for Fast JSONPath Queries on Diagram Snapshots
CREATE INDEX idx_diagrams_graph ON diagrams USING GIN (graph_snapshot);
