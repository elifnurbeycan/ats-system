package com.yasarbilgi.ats.pipeline.dto.response;

import java.util.List;

public record PipelineDetailResponseDto(
        Long id,
        String name,
        String code,
        String description,
        boolean defaultPipeline,
        boolean active,
        List<PipelineStageResponseDto> stages
) {}
