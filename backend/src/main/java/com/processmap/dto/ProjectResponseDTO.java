package com.processmap.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponseDTO(
    UUID id,
    UUID userId,
    String name,
    String domain,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
