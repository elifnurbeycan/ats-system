package com.yasarbilgi.ats.pipeline.dto.request;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import jakarta.validation.constraints.*;

public record UpdatePipelineStageRequestDto(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull PipelineStageType stageType
) {}
