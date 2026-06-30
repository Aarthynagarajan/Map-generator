package com.processmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequestDTO(
    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name cannot exceed 255 characters")
    String name,

    @NotBlank(message = "Domain is required")
    String domain,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description
) {}
