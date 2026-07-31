package com.yasarbilgi.ats.pipeline.dto.response;

public record PipelineSummaryResponseDto(
        Long id,
        String name,
        String code,
        String description,
        boolean defaultPipeline
) {
}
