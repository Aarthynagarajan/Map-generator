package com.processmap.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DiagramResponseDTO(
    UUID id,
    UUID projectId,
    Integer version,
    String promptText,
    String domain,
    List<LayoutNodeDTO> nodes,
    List<LayoutEdgeDTO> edges,
    String thumbnailUrl,
    boolean isCurrent,
    OffsetDateTime createdAt,
    GenerationMetadataDTO generationMetadata
) {}
