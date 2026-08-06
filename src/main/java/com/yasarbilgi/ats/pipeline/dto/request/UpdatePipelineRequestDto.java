package com.yasarbilgi.ats.pipeline.dto.request;

import jakarta.validation.constraints.*;

public record UpdatePipelineRequestDto(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        boolean defaultPipeline
) {}
