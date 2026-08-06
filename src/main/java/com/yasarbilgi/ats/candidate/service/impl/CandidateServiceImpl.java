package com.yasarbilgi.ats.candidate.service.impl;

import com.yasarbilgi.ats.candidate.dto.request.UpdateCandidateRequestDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateDetailResponseDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateProcessSummaryResponseDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateResponseDto;
import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.mapper.CandidateMapper;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidate.service.CandidateService;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateServiceImpl implements CandidateService {

    private final CompanyRepository companyRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final CandidateMapper candidateMapper;

    // Adayları en yeni kayıt önce olacak şekilde arar ve sayfalar.
    @Override
    public PageResponse<CandidateResponseDto> getAll(
            Long companyId,
            String search,
            int page,
            int size
    ) {
        validateCompany(companyId);
        validatePageRequest(page, size);

        Page<Candidate> candidates = candidateRepository.searchActiveCandidates(
                companyId,
                normalizeNullable(search),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return PageResponse.from(candidates, candidateMapper::toResponseDto);
    }

    // Aday profilini maaş bilgisi içermeyen süreç özetleriyle getirir.
    @Override
    public CandidateDetailResponseDto getById(Long companyId, Long candidateId) {
        Candidate candidate = getCandidate(companyId, candidateId);
        return buildDetailResponse(companyId, candidate);
    }

    // Aday profilini girilmesi zorunlu olmayan bilgilerle birlikte günceller.
    @Override
    @Transactional
    public CandidateDetailResponseDto update(
            Long companyId,
            Long candidateId,
            UpdateCandidateRequestDto request
    ) {
        Candidate candidate = getCandidate(companyId, candidateId);
        String linkedinUrl = normalizeNullable(request.linkedinUrl());
        validateLinkedinUrlIsAvailable(companyId, candidateId, linkedinUrl);

        candidate.updateIdentityInformation(
                request.firstName().trim(),
                request.lastName().trim(),
                linkedinUrl
        );
        candidate.updateContactInformation(
                normalizeEmail(request.email()),
                normalizeNullable(request.phone()),
                normalizeNullable(request.city())
        );
        candidate.updateProfessionalInformation(
                normalizeNullable(request.currentCompany()),
                normalizeNullable(request.currentJobTitle()),
                request.noticePeriodDays()
        );

        return buildDetailResponse(companyId, candidate);
    }

    // Aday detay yanıtını profil ve işe alım süreçlerinden oluşturur.
    private CandidateDetailResponseDto buildDetailResponse(
            Long companyId,
            Candidate candidate
    ) {
        List<CandidateProcessSummaryResponseDto> processes = candidateProcessRepository
                .findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
                        companyId,
                        candidate.getId()
                )
                .stream()
                .map(candidateMapper::toProcessSummaryResponseDto)
                .toList();

        return new CandidateDetailResponseDto(
                candidateMapper.toResponseDto(candidate),
                processes
        );
    }

    // İşlem yapılan şirketin aktif ve erişilebilir olduğunu doğrular.
    private void validateCompany(Long companyId) {
        companyRepository.findById(companyId)
                .filter(company -> company.isActive())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Şirket bulunamadı: " + companyId
                ));
    }

    // Sayfa numarası ve boyutunun API sınırları içerisinde olduğunu doğrular.
    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new BusinessRuleException("Sayfa numarası sıfırdan küçük olamaz.");
        }

        if (size < 1 || size > 100) {
            throw new BusinessRuleException("Sayfa boyutu 1 ile 100 arasında olmalıdır.");
        }
    }

    // Adayı şirket sınırı içerisinde ve aktif kayıtlardan getirir.
    private Candidate getCandidate(Long companyId, Long candidateId) {
        return candidateRepository.findByCompanyIdAndId(companyId, candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aday bulunamadı: " + candidateId
                ));
    }

    // LinkedIn adresinin aynı şirkette başka bir adaya ait olmadığını doğrular.
    private void validateLinkedinUrlIsAvailable(
            Long companyId,
            Long candidateId,
            String linkedinUrl
    ) {
        if (linkedinUrl == null) {
            return;
        }

        candidateRepository.findByCompanyIdAndLinkedinUrl(companyId, linkedinUrl)
                .filter(existingCandidate -> !existingCandidate.getId().equals(candidateId))
                .ifPresent(existingCandidate -> {
                    throw new BusinessRuleException(
                            "Bu LinkedIn adresi başka bir adayda kullanılıyor."
                    );
                });
    }

    // Boş metinleri null değerine dönüştürüp diğer metinleri kırpar.
    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    // E-posta adresini boş değilse küçük harfli standart biçime getirir.
    private String normalizeEmail(String email) {
        String normalizedEmail = normalizeNullable(email);
        return normalizedEmail == null
                ? null
                : normalizedEmail.toLowerCase(Locale.ROOT);
    }
}
