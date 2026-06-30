-- V5__seed_synonyms.sql: Seed >30 common engineering synonym terms for AI parser alias resolution

-- Industrial Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'pump', 'CENTRIFUGAL_PUMP', 'industrial'),
(gen_random_uuid(), 'water pump', 'CENTRIFUGAL_PUMP', 'industrial'),
(gen_random_uuid(), 'feed pump', 'CENTRIFUGAL_PUMP', 'industrial'),
(gen_random_uuid(), 'tank', 'STORAGE_TANK', 'industrial'),
(gen_random_uuid(), 'water tank', 'STORAGE_TANK', 'industrial'),
(gen_random_uuid(), 'holding tank', 'STORAGE_TANK', 'industrial'),
(gen_random_uuid(), 'valve', 'GATE_VALVE', 'industrial'),
(gen_random_uuid(), 'isolation valve', 'GATE_VALVE', 'industrial'),
(gen_random_uuid(), 'shutoff valve', 'GATE_VALVE', 'industrial'),
(gen_random_uuid(), 'non-return valve', 'CHECK_VALVE', 'industrial'),
(gen_random_uuid(), 'safety valve', 'PRESSURE_RELIEF_VALVE', 'industrial'),
(gen_random_uuid(), 'heat exchanger', 'HEAT_EXCHANGER', 'industrial'),
(gen_random_uuid(), 'cooler', 'HEAT_EXCHANGER', 'industrial'),
(gen_random_uuid(), 'flow sensor', 'FLOW_METER', 'industrial'),
(gen_random_uuid(), 'pressure sensor', 'PRESSURE_GAUGE', 'industrial'),
(gen_random_uuid(), 'temp gauge', 'TEMPERATURE_SENSOR', 'industrial'),
(gen_random_uuid(), 'level sensor', 'LEVEL_INDICATOR', 'industrial');

-- Electrical Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'breaker', 'CIRCUIT_BREAKER', 'electrical'),
(gen_random_uuid(), 'mcb', 'CIRCUIT_BREAKER', 'electrical'),
(gen_random_uuid(), 'mccb', 'CIRCUIT_BREAKER', 'electrical'),
(gen_random_uuid(), 'electric motor', 'MOTOR', 'electrical'),
(gen_random_uuid(), 'drive', 'VFD', 'electrical'),
(gen_random_uuid(), 'variable speed drive', 'VFD', 'electrical'),
(gen_random_uuid(), 'xfrmr', 'TRANSFORMER', 'electrical'),
(gen_random_uuid(), 'panel', 'PANEL_BOARD', 'electrical'),
(gen_random_uuid(), 'switchboard', 'PANEL_BOARD', 'electrical'),
(gen_random_uuid(), 'contactor', 'CONTACTOR', 'electrical'),
(gen_random_uuid(), 'backup battery', 'BATTERY', 'electrical');

-- Hydraulic Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'hydraulic pump', 'HYDRAULIC_PUMP', 'hydraulic'),
(gen_random_uuid(), 'fluid motor', 'HYDRAULIC_MOTOR', 'hydraulic'),
(gen_random_uuid(), 'ram', 'CYLINDER', 'hydraulic'),
(gen_random_uuid(), 'hydraulic ram', 'CYLINDER', 'hydraulic'),
(gen_random_uuid(), 'actuator', 'CYLINDER', 'hydraulic'),
(gen_random_uuid(), 'pressure valve', 'PRESSURE_RELIEF_HYD', 'hydraulic'),
(gen_random_uuid(), 'hydraulic reservoir', 'RESERVOIR_HYDRAULIC', 'hydraulic'),
(gen_random_uuid(), 'oil tank', 'RESERVOIR_HYDRAULIC', 'hydraulic');
