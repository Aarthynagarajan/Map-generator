package com.processmap.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
    GenerationMetadataDTO generationMetadata,
    JsonNode graphSnapshot
) {}

