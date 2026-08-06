package com.yasarbilgi.ats.dashboard.dto.response;

import java.time.Instant;
import java.util.List;

public record DashboardResponseDto(
        Instant generatedAt,
        Summary summary,
        List<StageDistribution> stageDistribution
) {
    public record Summary(
            long activeCandidateCount,
            long openPositionCount,
            long activeProcessCount,
            long upcomingInterviewCount,
            long pendingFollowUpCount,
            long overdueFollowUpCount
    ) {}

    public record StageDistribution(
            Long stageId,
            String stageName,
            String stageCode,
            Integer displayOrder,
            long candidateCount
    ) {}
}
