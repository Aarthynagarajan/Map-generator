package com.processmap.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShareResponseDTO(
    UUID id,
    UUID diagramId,
    String token,
    String shareUrl,
    OffsetDateTime expiresAt,
    boolean revoked,
    OffsetDateTime createdAt
) {}
