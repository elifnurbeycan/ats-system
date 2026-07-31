package com.yasarbilgi.ats.candidateprocess.dto.response;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;

import java.util.List;

public record PipelineBoardStageResponseDto(
        Long id,
        String name,
        String code,
        Integer displayOrder,
        PipelineStageType stageType,
        List<CandidateCardResponseDto> candidates
) {
}
