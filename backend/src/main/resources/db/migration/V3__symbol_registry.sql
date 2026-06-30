-- V3__symbol_registry.sql: Symbol registry and synonym dictionary tables

CREATE TABLE symbols (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol_id VARCHAR(100) NOT NULL,
    entity_class VARCHAR(100) NOT NULL,
    domain VARCHAR(20) NOT NULL,
    svg_path TEXT NOT NULL,
    default_tag_prefix VARCHAR(20),
    description TEXT,
    CONSTRAINT uq_symbols_symbol_id UNIQUE (symbol_id)
);

CREATE TABLE synonyms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    term VARCHAR(100) NOT NULL,
    entity_class VARCHAR(100) NOT NULL,
    domain VARCHAR(20) NOT NULL,
    CONSTRAINT uq_synonyms_term UNIQUE (term)
);

-- Composite B-Tree Index for rapid domain + entity class matching
CREATE INDEX idx_symbols_domain_class ON symbols(domain, entity_class);

-- B-Tree Index for synonym resolution
CREATE INDEX idx_synonyms_term ON synonyms(term);
