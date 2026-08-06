package com.yasarbilgi.ats.candidateprocess.dto.response;

import java.time.Instant;

public record CandidateStageHistoryResponseDto(
        Long id,
        Long fromStageId,
        String fromStageName,
        Long toStageId,
        String toStageName,
        String reason,
        Instant changedAt,
        Long changedBy
) {
}
