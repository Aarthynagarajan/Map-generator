package com.processmap.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String email,
    String displayName,
    String role,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
