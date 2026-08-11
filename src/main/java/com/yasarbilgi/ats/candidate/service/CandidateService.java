package com.yasarbilgi.ats.candidate.service;

import com.yasarbilgi.ats.candidate.dto.request.UpdateCandidateRequestDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateDetailResponseDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateResponseDto;
import com.yasarbilgi.ats.common.response.PageResponse;

public interface CandidateService {

    // Aktif adayları arama desteğiyle sayfalı olarak listeler.
    PageResponse<CandidateResponseDto> getAll(
            Long companyId,
            String search,
            boolean includeInactive,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    // Aday profilini aktif işe alım süreçleriyle birlikte getirir.
    CandidateDetailResponseDto getById(Long companyId, Long candidateId);

    // Adayın zorunlu ve isteğe bağlı profil bilgilerini günceller.
    CandidateDetailResponseDto update(
            Long companyId,
            Long candidateId,
            UpdateCandidateRequestDto request
    );

    // Adayı pasifleştirir (silme işlemi yerine yumuşak silme).
    void deactivate(Long companyId, Long candidateId);

    void activate(Long companyId, Long candidateId);
}
