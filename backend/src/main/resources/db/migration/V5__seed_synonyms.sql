-- V5__seed_synonyms.sql: Seed >30 common engineering synonym terms for AI parser alias resolution

-- Industrial Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'pump', 'CENTRIFUGAL_PUMP', 'INDUSTRIAL'),
(gen_random_uuid(), 'water pump', 'CENTRIFUGAL_PUMP', 'INDUSTRIAL'),
(gen_random_uuid(), 'feed pump', 'CENTRIFUGAL_PUMP', 'INDUSTRIAL'),
(gen_random_uuid(), 'tank', 'STORAGE_TANK', 'INDUSTRIAL'),
(gen_random_uuid(), 'water tank', 'STORAGE_TANK', 'INDUSTRIAL'),
(gen_random_uuid(), 'holding tank', 'STORAGE_TANK', 'INDUSTRIAL'),
(gen_random_uuid(), 'valve', 'GATE_VALVE', 'INDUSTRIAL'),
(gen_random_uuid(), 'isolation valve', 'GATE_VALVE', 'INDUSTRIAL'),
(gen_random_uuid(), 'shutoff valve', 'GATE_VALVE', 'INDUSTRIAL'),
(gen_random_uuid(), 'non-return valve', 'CHECK_VALVE', 'INDUSTRIAL'),
(gen_random_uuid(), 'safety valve', 'PRESSURE_RELIEF_VALVE', 'INDUSTRIAL'),
(gen_random_uuid(), 'heat exchanger', 'HEAT_EXCHANGER', 'INDUSTRIAL'),
(gen_random_uuid(), 'cooler', 'HEAT_EXCHANGER', 'INDUSTRIAL'),
(gen_random_uuid(), 'flow sensor', 'FLOW_METER', 'INDUSTRIAL'),
(gen_random_uuid(), 'pressure sensor', 'PRESSURE_GAUGE', 'INDUSTRIAL'),
(gen_random_uuid(), 'temp gauge', 'TEMPERATURE_SENSOR', 'INDUSTRIAL'),
(gen_random_uuid(), 'level sensor', 'LEVEL_INDICATOR', 'INDUSTRIAL');

-- Electrical Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'breaker', 'CIRCUIT_BREAKER', 'ELECTRICAL'),
(gen_random_uuid(), 'mcb', 'CIRCUIT_BREAKER', 'ELECTRICAL'),
(gen_random_uuid(), 'mccb', 'CIRCUIT_BREAKER', 'ELECTRICAL'),
(gen_random_uuid(), 'electric motor', 'MOTOR', 'ELECTRICAL'),
(gen_random_uuid(), 'drive', 'VFD', 'ELECTRICAL'),
(gen_random_uuid(), 'variable speed drive', 'VFD', 'ELECTRICAL'),
(gen_random_uuid(), 'xfrmr', 'TRANSFORMER', 'ELECTRICAL'),
(gen_random_uuid(), 'panel', 'PANEL_BOARD', 'ELECTRICAL'),
(gen_random_uuid(), 'switchboard', 'PANEL_BOARD', 'ELECTRICAL'),
(gen_random_uuid(), 'contactor', 'CONTACTOR', 'ELECTRICAL'),
(gen_random_uuid(), 'backup battery', 'BATTERY', 'ELECTRICAL');

-- Hydraulic Synonyms
INSERT INTO synonyms (id, term, entity_class, domain) VALUES
(gen_random_uuid(), 'hydraulic pump', 'HYDRAULIC_PUMP', 'HYDRAULIC'),
(gen_random_uuid(), 'fluid motor', 'HYDRAULIC_MOTOR', 'HYDRAULIC'),
(gen_random_uuid(), 'ram', 'CYLINDER', 'HYDRAULIC'),
(gen_random_uuid(), 'hydraulic ram', 'CYLINDER', 'HYDRAULIC'),
(gen_random_uuid(), 'actuator', 'CYLINDER', 'HYDRAULIC'),
(gen_random_uuid(), 'pressure valve', 'PRESSURE_RELIEF_HYD', 'HYDRAULIC'),
(gen_random_uuid(), 'hydraulic reservoir', 'RESERVOIR_HYDRAULIC', 'HYDRAULIC'),
(gen_random_uuid(), 'oil tank', 'RESERVOIR_HYDRAULIC', 'HYDRAULIC');
