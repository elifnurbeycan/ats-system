package com.yasarbilgi.ats.candidate.dto.response;

import java.time.Instant;

public record CandidateProcessSummaryResponseDto(
        Long id,
        Long positionId,
        String positionTitle,
        Long pipelineId,
        String pipelineName,
        Long currentStageId,
        String currentStageName,
        Instant completedAt
) {
}
