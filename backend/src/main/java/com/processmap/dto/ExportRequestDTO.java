package com.processmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ExportRequestDTO(
    @NotNull(message = "Diagram ID is required")
    UUID diagramId,

    @NotBlank(message = "Format is required")
    @Pattern(regexp = "^(PNG|SVG|JSON)$", message = "Format must be one of [PNG, SVG, JSON]")
    String format
) {}
