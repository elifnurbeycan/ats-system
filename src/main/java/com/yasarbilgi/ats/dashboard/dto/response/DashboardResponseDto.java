package com.yasarbilgi.ats.dashboard.dto.response;

import java.time.Instant;
import java.util.List;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;

public record DashboardResponseDto(
        Instant generatedAt,
        Summary summary,
        List<StageDistribution> stageDistribution,
        PeriodAnalytics weeklyAnalytics,
        PeriodAnalytics monthlyAnalytics,
        PeriodAnalytics allTimeAnalytics,
        List<MonthlyApplicationTrend> monthlyApplicationTrend,
        List<DepartmentDistribution> departmentDistribution
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
            PipelineStageType stageType,
            long candidateCount
    ) {}

    public record PeriodAnalytics(
            Instant periodStart,
            long newCandidateCount,
            long newApplicationCount,
            long openedPositionCount,
            long hiredCount,
            long rejectedCount
    ) {}

    public record MonthlyApplicationTrend(
            Instant monthStart,
            long applicationCount
    ) {}

    public record DepartmentDistribution(
            Long departmentId,
            String departmentName,
            long applicationCount
    ) {}
}
