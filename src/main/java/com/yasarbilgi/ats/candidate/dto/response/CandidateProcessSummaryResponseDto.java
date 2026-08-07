package com.yasarbilgi.ats.candidate.dto.response;

import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;

import java.time.Instant;

public record CandidateProcessSummaryResponseDto(
        Long id,
        Long positionId,
        String positionTitle,
        Long departmentId,
        String departmentName,
        Long pipelineId,
        String pipelineName,
        Long currentStageId,
        String currentStageName,
        PipelineStageType currentStageType,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
