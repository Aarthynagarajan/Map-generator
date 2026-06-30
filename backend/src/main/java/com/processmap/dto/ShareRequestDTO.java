package com.processmap.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShareRequestDTO(
    @NotNull(message = "Diagram ID is required")
    UUID diagramId
) {}
