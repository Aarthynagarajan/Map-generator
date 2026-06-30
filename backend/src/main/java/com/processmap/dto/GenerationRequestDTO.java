package com.processmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GenerationRequestDTO(
    @NotNull(message = "Project ID is required")
    UUID projectId,

    @NotBlank(message = "Prompt text is required")
    @Size(min = 20, max = 32000, message = "Prompt must be between 20 and 32000 characters")
    String prompt,

    @NotBlank(message = "Domain is required")
    String domain,

    GenerationConstraints constraints
) {}
