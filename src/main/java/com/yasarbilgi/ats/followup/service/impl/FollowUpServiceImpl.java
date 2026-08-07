package com.yasarbilgi.ats.followup.service.impl;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.followup.dto.request.*;
import com.yasarbilgi.ats.followup.dto.response.FollowUpResponseDto;
import com.yasarbilgi.ats.followup.entity.*;
import com.yasarbilgi.ats.followup.mapper.FollowUpMapper;
import com.yasarbilgi.ats.followup.repository.FollowUpRepository;
import com.yasarbilgi.ats.followup.service.FollowUpService;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class FollowUpServiceImpl implements FollowUpService {
    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository processRepository;
    private final UserRepository userRepository;
    private final FollowUpRepository followUpRepository;
    private final FollowUpMapper mapper;

    // Takip görevini doğrulanmış aday, süreç ve şirket kullanıcısıyla oluşturur.
    @Override @Transactional
    public FollowUpResponseDto create(Long companyId, Long candidateId, CreateFollowUpRequestDto request) {
        Candidate candidate = getCandidate(companyId, candidateId);
        CandidateProcess process = getProcess(companyId, candidateId, request.candidateProcessId());
        User assignedTo = getUser(companyId, request.assignedToUserId());
        validateFutureDueAt(request.dueAt());
        FollowUp followUp = FollowUp.builder().company(candidate.getCompany()).candidate(candidate)
                .candidateProcess(process).assignedTo(assignedTo).title(request.title().trim())
                .description(text(request.description())).dueAt(request.dueAt())
                .status(FollowUpStatus.PENDING).build();
        return mapper.toResponseDto(followUpRepository.save(followUp));
    }
    // Aday görevlerini isteğe bağlı durum ve sorumlu filtresiyle getirir.
    @Override public PageResponse<FollowUpResponseDto> getAll(Long companyId, Long candidateId,
                                                              FollowUpStatus status, Long assignedToUserId,
                                                              int page, int size) {
        getCandidate(companyId, candidateId);
        if (page < 0 || size < 1 || size > 200) throw new BusinessRuleException("Geçersiz sayfalama bilgisi.");
        var result = followUpRepository.searchActive(companyId, candidateId, status, assignedToUserId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueAt")));
        return PageResponse.from(result, mapper::toResponseDto);
    }
    // Yalnızca bekleyen takip görevinin planlama bilgilerini günceller.
    @Override @Transactional
    public FollowUpResponseDto update(Long companyId, Long candidateId, Long id, UpdateFollowUpRequestDto request) {
        FollowUp followUp = getFollowUp(companyId, candidateId, id);
        ensurePending(followUp);
        validateFutureDueAt(request.dueAt());
        followUp.updateDetails(request.title().trim(), text(request.description()), request.dueAt(),
                getUser(companyId, request.assignedToUserId()));
        return mapper.toResponseDto(followUp);
    }
    // Bekleyen görevi tamamlandı veya iptal edildi durumuna geçirir.
    @Override @Transactional
    public FollowUpResponseDto changeStatus(Long companyId, Long candidateId, Long id,
                                             ChangeFollowUpStatusRequestDto request) {
        FollowUp followUp = getFollowUp(companyId, candidateId, id);
        if (followUp.getStatus() == request.status()) return mapper.toResponseDto(followUp);
        ensurePending(followUp);
        switch (request.status()) {
            case COMPLETED -> followUp.complete();
            case CANCELLED -> followUp.cancel();
            case PENDING -> throw new BusinessRuleException("Görev yeniden bekleyen durumuna alınamaz.");
        }
        return mapper.toResponseDto(followUp);
    }
    // Adayı şirket sınırında aktif kayıtlardan getirir.
    private Candidate getCandidate(Long companyId, Long id) { return candidateRepository.findByCompanyIdAndId(companyId, id)
            .filter(Candidate::isActive).orElseThrow(() -> new ResourceNotFoundException("Aday bulunamadı.")); }
    // Seçilmişse sürecin aynı aday ve şirkete ait olduğunu doğrular.
    private CandidateProcess getProcess(Long companyId, Long candidateId, Long id) { if (id == null) return null;
        return processRepository.findByCompanyIdAndId(companyId, id).filter(CandidateProcess::isActive)
                .filter(p -> p.getCandidate().getId().equals(candidateId))
                .orElseThrow(() -> new ResourceNotFoundException("Aday süreci bulunamadı veya bu adaya ait değil.")); }
    // Sorumlu kullanıcıyı şirket sınırında aktif kayıtlardan getirir.
    private User getUser(Long companyId, Long id) { return userRepository.findByCompanyIdAndId(companyId, id)
            .filter(User::isActive).orElseThrow(() -> new ResourceNotFoundException("Sorumlu kullanıcı bulunamadı.")); }
    // Takip görevini şirket ve aday sınırında aktif kayıtlardan getirir.
    private FollowUp getFollowUp(Long companyId, Long candidateId, Long id) { return followUpRepository
            .findWithDetailsByCompanyIdAndCandidateIdAndId(companyId, candidateId, id).filter(FollowUp::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Takip görevi bulunamadı.")); }
    // Görev üzerinde değişiklik yapılabilmesi için bekleyen durumda olmasını doğrular.
    private void ensurePending(FollowUp item) { if (item.getStatus() != FollowUpStatus.PENDING)
        throw new BusinessRuleException("Yalnızca bekleyen takip görevi değiştirilebilir."); }
    // Yeni veya güncellenen son tarihin gelecekte olmasını doğrular.
    private void validateFutureDueAt(Instant dueAt) { if (!dueAt.isAfter(Instant.now()))
        throw new BusinessRuleException("Takip görevinin son tarihi gelecekte olmalıdır."); }
    // Boş isteğe bağlı metinleri null değerine dönüştürür.
    private String text(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
