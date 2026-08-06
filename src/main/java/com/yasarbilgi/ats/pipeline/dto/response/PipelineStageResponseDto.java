package com.yasarbilgi.ats.pipeline.dto.response;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;

public record PipelineStageResponseDto(
        Long id,
        String name,
        String code,
        String description,
        Integer displayOrder,
        PipelineStageType stageType,
        boolean active
) {
}
