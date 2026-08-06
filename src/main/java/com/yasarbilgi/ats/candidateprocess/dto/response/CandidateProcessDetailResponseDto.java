package com.yasarbilgi.ats.candidateprocess.dto.response;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;

import java.time.Instant;

public record CandidateProcessDetailResponseDto(
        Long id,
        Long candidateId,
        String candidateFullName,
        Long departmentId,
        String departmentName,
        Long positionId,
        String positionTitle,
        Long pipelineId,
        String pipelineName,
        Long currentStageId,
        String currentStageName,
        PipelineStageType currentStageType,
        Instant completedAt,
        boolean active
) {
}
