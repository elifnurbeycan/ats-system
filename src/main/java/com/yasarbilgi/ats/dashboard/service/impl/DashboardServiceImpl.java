package com.yasarbilgi.ats.dashboard.service.impl;

import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessStageHistoryRepository;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import com.yasarbilgi.ats.dashboard.dto.response.DashboardResponseDto;
import com.yasarbilgi.ats.dashboard.service.DashboardService;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import com.yasarbilgi.ats.followup.repository.FollowUpRepository;
import com.yasarbilgi.ats.interview.entity.InterviewStatus;
import com.yasarbilgi.ats.interview.repository.InterviewRepository;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import com.yasarbilgi.ats.position.repository.PositionRepository;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import com.yasarbilgi.ats.security.service.DataScopeService;
import com.yasarbilgi.ats.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final PositionRepository positionRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final CandidateProcessStageHistoryRepository stageHistoryRepository;
    private final InterviewRepository interviewRepository;
    private final FollowUpRepository followUpRepository;
    private final DataScopeService dataScopeService;

    // Şirketin temel metriklerini ve adayların aşama dağılımını tek yanıtta oluşturur.
    @Override
    public DashboardResponseDto getSummary(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Şirket bulunamadı: " + companyId);
        }

        Instant now = Instant.now();
        if (!dataScopeService.hasCompanyScope()) {
            return getDepartmentSummary(companyId, now);
        }
        DashboardResponseDto.Summary summary = new DashboardResponseDto.Summary(
                candidateRepository.countByCompanyIdAndActiveTrue(companyId),
                positionRepository.countByCompanyIdAndStatusAndActiveTrue(companyId, PositionStatus.OPEN),
                candidateProcessRepository.countByCompanyIdAndActiveTrueAndCompletedAtIsNull(companyId),
                interviewRepository.countByCompanyIdAndStatusAndScheduledAtBetweenAndActiveTrue(
                        companyId, InterviewStatus.SCHEDULED, now, now.plus(7, ChronoUnit.DAYS)),
                followUpRepository.countByCompanyIdAndStatusAndActiveTrue(companyId, FollowUpStatus.PENDING),
                followUpRepository.countByCompanyIdAndStatusAndDueAtBeforeAndActiveTrue(
                        companyId, FollowUpStatus.PENDING, now)
        );

        var stageDistribution = candidateProcessRepository.countActiveProcessesByStage(companyId).stream()
                .map(stage -> new DashboardResponseDto.StageDistribution(
                        stage.getStageId(), stage.getStageName(), stage.getStageCode(),
                        stage.getDisplayOrder(), stage.getCandidateCount()))
                .toList();
        return new DashboardResponseDto(now, summary, stageDistribution,
                getPeriodAnalytics(companyId, now.minus(7, ChronoUnit.DAYS), null),
                getPeriodAnalytics(companyId, now.minus(30, ChronoUnit.DAYS), null));
    }

    // Dashboard metriklerini yalnızca kullanıcının yönettiği departmanlardan oluşturur.
    private DashboardResponseDto getDepartmentSummary(Long companyId, Instant now) {
        var departmentIds = dataScopeService.getManagedDepartmentIds();
        if (departmentIds.isEmpty()) {
            throw new ForbiddenException("Yönetilen aktif bir departman bulunmuyor.");
        }
        DashboardResponseDto.Summary summary = new DashboardResponseDto.Summary(
                candidateRepository.countActiveCandidatesByDepartmentIds(companyId, departmentIds),
                positionRepository.countByCompanyIdAndDepartmentIdInAndStatusAndActiveTrue(
                        companyId, departmentIds, PositionStatus.OPEN),
                candidateProcessRepository.countByCompanyIdAndPositionDepartmentIdInAndActiveTrueAndCompletedAtIsNull(
                        companyId, departmentIds),
                interviewRepository
                        .countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndScheduledAtBetweenAndActiveTrue(
                                companyId, departmentIds, InterviewStatus.SCHEDULED,
                                now, now.plus(7, ChronoUnit.DAYS)),
                followUpRepository.countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndActiveTrue(
                        companyId, departmentIds, FollowUpStatus.PENDING),
                followUpRepository
                        .countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndDueAtBeforeAndActiveTrue(
                                companyId, departmentIds, FollowUpStatus.PENDING, now));
        var stages = candidateProcessRepository
                .countActiveProcessesByStageAndDepartmentIds(companyId, departmentIds).stream()
                .map(stage -> new DashboardResponseDto.StageDistribution(stage.getStageId(), stage.getStageName(),
                        stage.getStageCode(), stage.getDisplayOrder(), stage.getCandidateCount())).toList();
        return new DashboardResponseDto(now, summary, stages,
                getPeriodAnalytics(companyId, now.minus(7, ChronoUnit.DAYS), departmentIds),
                getPeriodAnalytics(companyId, now.minus(30, ChronoUnit.DAYS), departmentIds));
    }

    private DashboardResponseDto.PeriodAnalytics getPeriodAnalytics(
            Long companyId, Instant periodStart, Set<Long> departmentIds) {
        boolean companyScope = departmentIds == null;
        long newCandidates = companyScope
                ? candidateRepository.countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(companyId, periodStart)
                : candidateRepository.countNewCandidatesByDepartmentIds(companyId, departmentIds, periodStart);
        long newApplications = companyScope
                ? candidateProcessRepository.countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(companyId, periodStart)
                : candidateProcessRepository.countByCompanyIdAndPositionDepartmentIdInAndActiveTrueAndCreatedAtGreaterThanEqual(
                        companyId, departmentIds, periodStart);
        long openedPositions = companyScope
                ? positionRepository.countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(companyId, periodStart)
                : positionRepository.countByCompanyIdAndDepartmentIdInAndActiveTrueAndCreatedAtGreaterThanEqual(
                        companyId, departmentIds, periodStart);
        long hired = companyScope
                ? stageHistoryRepository.countTransitionsByStageType(companyId, periodStart, PipelineStageType.HIRED)
                : stageHistoryRepository.countTransitionsByStageTypeAndDepartmentIds(
                        companyId, departmentIds, periodStart, PipelineStageType.HIRED);
        long rejected = companyScope
                ? stageHistoryRepository.countTransitionsByStageType(companyId, periodStart, PipelineStageType.REJECTED)
                : stageHistoryRepository.countTransitionsByStageTypeAndDepartmentIds(
                        companyId, departmentIds, periodStart, PipelineStageType.REJECTED);
        return new DashboardResponseDto.PeriodAnalytics(
                periodStart, newCandidates, newApplications, openedPositions, hired, rejected);
    }
}
