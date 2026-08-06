package com.yasarbilgi.ats.dashboard.service.impl;

import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final PositionRepository positionRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final InterviewRepository interviewRepository;
    private final FollowUpRepository followUpRepository;

    // Şirketin temel metriklerini ve adayların aşama dağılımını tek yanıtta oluşturur.
    @Override
    public DashboardResponseDto getSummary(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Şirket bulunamadı: " + companyId);
        }

        Instant now = Instant.now();
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
        return new DashboardResponseDto(now, summary, stageDistribution);
    }
}
