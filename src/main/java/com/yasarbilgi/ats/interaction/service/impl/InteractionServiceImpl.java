package com.yasarbilgi.ats.interaction.service.impl;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import com.yasarbilgi.ats.interaction.dto.request.CreateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.request.UpdateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.response.InteractionResponseDto;
import com.yasarbilgi.ats.interaction.entity.Interaction;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import com.yasarbilgi.ats.interaction.mapper.InteractionMapper;
import com.yasarbilgi.ats.interaction.repository.InteractionRepository;
import com.yasarbilgi.ats.interaction.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InteractionServiceImpl implements InteractionService {

    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final InteractionRepository interactionRepository;
    private final InteractionMapper interactionMapper;

    // İletişimi adaya ve seçilmişse doğrulanan aday sürecine bağlayarak oluşturur.
    @Override
    @Transactional
    public InteractionResponseDto create(
            Long companyId,
            Long candidateId,
            CreateInteractionRequestDto request
    ) {
        Candidate candidate = getCandidate(companyId, candidateId);
        CandidateProcess process = getCandidateProcess(
                companyId,
                candidateId,
                request.candidateProcessId()
        );

        Interaction interaction = Interaction.builder()
                .company(candidate.getCompany())
                .candidate(candidate)
                .candidateProcess(process)
                .channel(request.channel())
                .direction(request.direction())
                .occurredAt(request.occurredAt() == null ? Instant.now() : request.occurredAt())
                .subject(normalizeNullable(request.subject()))
                .summary(request.summary().trim())
                .build();

        return interactionMapper.toResponseDto(interactionRepository.save(interaction));
    }

    // İletişimleri isteğe bağlı süreç ve kanal filtreleriyle en yeniden eskiye getirir.
    @Override
    public List<InteractionResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId,
            InteractionChannel channel
    ) {
        getCandidate(companyId, candidateId);

        List<Interaction> interactions;
        if (candidateProcessId == null) {
            interactions = interactionRepository
                    .findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByOccurredAtDesc(
                            companyId,
                            candidateId
                    );
        } else {
            getCandidateProcess(companyId, candidateId, candidateProcessId);
            interactions = interactionRepository
                    .findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndActiveTrueOrderByOccurredAtDesc(
                            companyId,
                            candidateId,
                            candidateProcessId
                    );
        }

        return interactions.stream()
                .filter(interaction -> channel == null || interaction.getChannel() == channel)
                .map(interactionMapper::toResponseDto)
                .toList();
    }

    // Aktif iletişim kaydının kanal, yön, zaman ve metin bilgilerini günceller.
    @Override
    @Transactional
    public InteractionResponseDto update(
            Long companyId,
            Long candidateId,
            Long interactionId,
            UpdateInteractionRequestDto request
    ) {
        Interaction interaction = getInteraction(companyId, candidateId, interactionId);
        interaction.updateDetails(
                request.channel(),
                request.direction(),
                request.occurredAt(),
                normalizeNullable(request.subject()),
                request.summary().trim()
        );
        return interactionMapper.toResponseDto(interaction);
    }

    // İletişim kaydını geçmiş korunacak şekilde pasifleştirir.
    @Override
    @Transactional
    public InteractionResponseDto deactivate(
            Long companyId,
            Long candidateId,
            Long interactionId
    ) {
        Interaction interaction = getInteraction(companyId, candidateId, interactionId);
        interaction.deactivate();
        return interactionMapper.toResponseDto(interaction);
    }

    // Adayı şirket sınırı içerisinde aktif kayıtlardan getirir.
    private Candidate getCandidate(Long companyId, Long candidateId) {
        return candidateRepository.findByCompanyIdAndId(companyId, candidateId)
                .filter(Candidate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aday bulunamadı: " + candidateId
                ));
    }

    // Süreç seçilmişse sürecin aynı şirkete ve adaya ait olduğunu doğrular.
    private CandidateProcess getCandidateProcess(
            Long companyId,
            Long candidateId,
            Long candidateProcessId
    ) {
        if (candidateProcessId == null) {
            return null;
        }

        return candidateProcessRepository
                .findByCompanyIdAndId(companyId, candidateProcessId)
                .filter(CandidateProcess::isActive)
                .filter(process -> process.getCandidate().getId().equals(candidateId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aday süreci bulunamadı veya bu adaya ait değil."
                ));
    }

    // İletişim kaydını şirket ve aday sınırı içerisinde aktif kayıtlardan getirir.
    private Interaction getInteraction(
            Long companyId,
            Long candidateId,
            Long interactionId
    ) {
        return interactionRepository
                .findByCompanyIdAndCandidateIdAndId(companyId, candidateId, interactionId)
                .filter(Interaction::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "İletişim kaydı bulunamadı: " + interactionId
                ));
    }

    // Boş metinleri null değerine dönüştürüp diğer metinleri kırpar.
    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
