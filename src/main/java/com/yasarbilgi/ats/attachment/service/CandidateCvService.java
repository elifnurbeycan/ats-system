package com.yasarbilgi.ats.attachment.service;

import com.yasarbilgi.ats.attachment.dto.CandidateCvResponseDto;
import com.yasarbilgi.ats.attachment.entity.CandidateCv;
import com.yasarbilgi.ats.attachment.repository.CandidateCvRepository;
import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.ForbiddenException;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.security.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateCvService {
    private final CandidateCvRepository repository;
    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final CandidateCvStorageService storage;
    private final DataScopeService dataScopeService;

    public CandidateCvResponseDto getMetadata(Long companyId, Long candidateId) {
        requireCandidate(companyId, candidateId);
        return toDto(requireCv(companyId, candidateId));
    }

    public DownloadedCv download(Long companyId, Long candidateId) {
        requireCandidate(companyId, candidateId);
        CandidateCv cv = requireCv(companyId, candidateId);
        return new DownloadedCv(cv.getOriginalFileName(), cv.getContentType(), storage.load(cv.getStorageKey()));
    }

    @Transactional
    public CandidateCvResponseDto upload(Long companyId, Long candidateId, MultipartFile file) {
        Candidate candidate = requireCandidate(companyId, candidateId);
        CandidateCvStorageService.StoredCv stored = storage.store(file);
        CandidateCv cv = repository.findByCompanyIdAndCandidateId(companyId, candidateId).orElse(null);
        String oldKey = null;
        if (cv == null) {
            cv = CandidateCv.builder()
                    .company(candidate.getCompany()).candidate(candidate)
                    .originalFileName(stored.fileName()).contentType(stored.contentType())
                    .fileSize(stored.fileSize()).storageKey(stored.storageKey()).build();
        } else {
            oldKey = cv.getStorageKey();
            cv.replace(stored.fileName(), stored.contentType(), stored.fileSize(), stored.storageKey());
        }
        CandidateCv saved = repository.save(cv);
        if (oldKey != null) storage.delete(oldKey);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long companyId, Long candidateId) {
        requireCandidate(companyId, candidateId);
        CandidateCv cv = requireCv(companyId, candidateId);
        repository.delete(cv);
        repository.flush();
        storage.delete(cv.getStorageKey());
    }

    private Candidate requireCandidate(Long companyId, Long candidateId) {
        Candidate candidate = candidateRepository.findByCompanyIdAndId(companyId, candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Aday bulunamadı: " + candidateId));
        if (!dataScopeService.hasCompanyScope()) {
            Set<Long> departments = dataScopeService.getManagedDepartmentIds();
            if (departments.isEmpty() || !candidateProcessRepository
                    .existsByCompanyIdAndCandidateIdAndPositionDepartmentIdInAndActiveTrue(companyId, candidateId, departments)) {
                throw new ForbiddenException("Bu adayın CV bilgisine erişim yetkiniz bulunmuyor.");
            }
        }
        return candidate;
    }

    private CandidateCv requireCv(Long companyId, Long candidateId) {
        return repository.findByCompanyIdAndCandidateId(companyId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Adaya ait CV bulunamadı."));
    }

    private CandidateCvResponseDto toDto(CandidateCv cv) {
        return new CandidateCvResponseDto(cv.getId(), cv.getCandidate().getId(), cv.getOriginalFileName(),
                cv.getContentType(), cv.getFileSize(), cv.getUpdatedAt());
    }

    public record DownloadedCv(String fileName, String contentType, Resource resource) {}
}
