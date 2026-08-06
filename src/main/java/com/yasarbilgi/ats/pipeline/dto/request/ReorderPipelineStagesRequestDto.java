package com.yasarbilgi.ats.pipeline.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record ReorderPipelineStagesRequestDto(
        @NotEmpty List<@NotNull Long> stageIds
) {}
