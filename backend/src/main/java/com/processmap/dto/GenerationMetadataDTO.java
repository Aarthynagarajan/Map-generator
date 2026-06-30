package com.processmap.dto;

public record GenerationMetadataDTO(
    String modelUsed,
    Integer promptTokens,
    Integer completionTokens,
    Long latencyMs
) {}
