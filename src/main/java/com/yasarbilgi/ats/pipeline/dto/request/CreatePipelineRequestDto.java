package com.yasarbilgi.ats.pipeline.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreatePipelineRequestDto(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String code,
        @Size(max = 500) String description,
        boolean defaultPipeline,
        @NotEmpty List<@Valid CreatePipelineStageRequestDto> stages
) {}
