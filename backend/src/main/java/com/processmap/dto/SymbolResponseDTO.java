package com.processmap.dto;

import java.util.UUID;

public record SymbolResponseDTO(
    UUID id,
    String symbolId,
    String entityClass,
    String domain,
    String svgPath,
    String defaultTagPrefix,
    String description
) {}
