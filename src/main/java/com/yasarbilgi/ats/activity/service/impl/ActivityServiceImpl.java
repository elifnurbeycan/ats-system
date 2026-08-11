package com.yasarbilgi.ats.activity.service.impl;

import com.yasarbilgi.ats.activity.dto.response.ActivityResponseDto;
import com.yasarbilgi.ats.activity.entity.ActivityType;
import com.yasarbilgi.ats.activity.repository.ActivityTimelineRepository;
import com.yasarbilgi.ats.activity.service.ActivityService;
import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ForbiddenException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.security.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {

    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository processRepository;
    private final ActivityTimelineRepository timelineRepository;
    private final DataScopeService dataScopeService;

    @Override
    public PageResponse<ActivityResponseDto> getTimeline(Long companyId, Long candidateId,
                                                         ActivityType type, int page, int size) {
        validatePageRequest(page, size);
        getCandidate(companyId, candidateId);

        boolean companyWide = dataScopeService.hasCompanyScope();
        Set<Long> departmentIds = companyWide ? Set.of() : dataScopeService.getManagedDepartmentIds();
        if (!companyWide && (departmentIds.isEmpty()
                || !processRepository.existsByCompanyIdAndCandidateIdAndPositionDepartmentIdInAndActiveTrue(
                companyId, candidateId, departmentIds))) {
            throw new ForbiddenException("Bu adayın aktivite geçmişine erişim yetkiniz bulunmuyor.");
        }

        ActivityTimelineRepository.TimelinePage result = timelineRepository.findTimeline(
                companyId, candidateId, type, page, size, companyWide, departmentIds);
        int totalPages = result.totalElements() == 0
                ? 0
                : (int) Math.ceil((double) result.totalElements() / size);

        return new PageResponse<>(
                result.content(),
                page,
                size,
                result.totalElements(),
                totalPages,
                page == 0,
                totalPages == 0 || page >= totalPages - 1
        );
    }

    private Candidate getCandidate(Long companyId, Long candidateId) {
        return candidateRepository.findByCompanyIdAndId(companyId, candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Aday bulunamadı."));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessRuleException("Aktivite sayfa boyutu 1 ile 100 arasında olmalıdır.");
        }
    }
}
