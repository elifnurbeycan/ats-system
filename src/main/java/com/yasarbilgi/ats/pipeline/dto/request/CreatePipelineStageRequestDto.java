package com.yasarbilgi.ats.pipeline.dto.request;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import jakarta.validation.constraints.*;

public record CreatePipelineStageRequestDto(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String code,
        @Size(max = 500) String description,
        @NotNull @Positive Integer displayOrder,
        @NotNull PipelineStageType stageType
) {}
