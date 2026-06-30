package com.processmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record ScenarioRequestDTO(
    @NotNull(message = "Diagram ID is required")
    UUID diagramId,

    @NotBlank(message = "Scenario name is required")
    @Size(max = 100, message = "Scenario name cannot exceed 100 characters")
    String name,

    @NotNull(message = "Stopper states cannot be null")
    Map<String, String> stopperStates,

    boolean isDefault
) {}
