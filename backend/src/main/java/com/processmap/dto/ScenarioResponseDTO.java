package com.processmap.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ScenarioResponseDTO(
    UUID id,
    UUID diagramId,
    String name,
    Map<String, String> stopperStates,
    boolean isDefault,
    OffsetDateTime createdAt
) {}
