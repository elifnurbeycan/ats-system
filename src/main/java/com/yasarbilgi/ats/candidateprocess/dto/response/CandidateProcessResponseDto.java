package com.yasarbilgi.ats.candidateprocess.dto.response;

import java.time.Instant;

public record CandidateProcessResponseDto(
        Long id,
        Long candidateId,
        String candidateFullName,
        Long positionId,
        Long pipelineId,
        Long currentStageId,
        String currentStageName,
        Instant createdAt,
        Instant updatedAt
) {
}
