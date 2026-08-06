package com.yasarbilgi.ats.candidatenote.service.impl;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidate.repository.CandidateRepository;
import com.yasarbilgi.ats.candidatenote.dto.request.CreateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.request.UpdateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.response.CandidateNoteResponseDto;
import com.yasarbilgi.ats.candidatenote.entity.CandidateNote;
import com.yasarbilgi.ats.candidatenote.mapper.CandidateNoteMapper;
import com.yasarbilgi.ats.candidatenote.repository.CandidateNoteRepository;
import com.yasarbilgi.ats.candidatenote.service.CandidateNoteService;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.repository.CandidateProcessRepository;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateNoteServiceImpl implements CandidateNoteService {

    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final CandidateNoteRepository candidateNoteRepository;
    private final CandidateNoteMapper candidateNoteMapper;

    // Aday notunu genel profile veya doğrulanmış aday sürecine bağlayarak oluşturur.
    @Override
    @Transactional
    public CandidateNoteResponseDto create(
            Long companyId,
            Long candidateId,
            CreateCandidateNoteRequestDto request
    ) {
        Candidate candidate = getCandidate(companyId, candidateId);
        CandidateProcess process = getCandidateProcess(
                companyId,
                candidateId,
                request.candidateProcessId()
        );

        CandidateNote note = CandidateNote.builder()
                .company(candidate.getCompany())
                .candidate(candidate)
                .candidateProcess(process)
                .content(request.content().trim())
                .build();

        return candidateNoteMapper.toResponseDto(candidateNoteRepository.save(note));
    }

    // Adayın genel ve süreç notlarını veya yalnızca seçilen sürecin notlarını getirir.
    @Override
    public List<CandidateNoteResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId
    ) {
        getCandidate(companyId, candidateId);

        List<CandidateNote> notes;
        if (candidateProcessId == null) {
            notes = candidateNoteRepository
                    .findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
                            companyId,
                            candidateId
                    );
        } else {
            getCandidateProcess(companyId, candidateId, candidateProcessId);
            notes = candidateNoteRepository
                    .findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndActiveTrueOrderByCreatedAtDesc(
                            companyId,
                            candidateId,
                            candidateProcessId
                    );
        }

        return notes.stream()
                .map(candidateNoteMapper::toResponseDto)
                .toList();
    }

    // Aktif aday notunun metin içeriğini günceller.
    @Override
    @Transactional
    public CandidateNoteResponseDto update(
            Long companyId,
            Long candidateId,
            Long noteId,
            UpdateCandidateNoteRequestDto request
    ) {
        CandidateNote note = getNote(companyId, candidateId, noteId);
        note.updateContent(request.content().trim());
        return candidateNoteMapper.toResponseDto(note);
    }

    // Aday notunu geçmiş kaydı korunacak şekilde pasifleştirir.
    @Override
    @Transactional
    public CandidateNoteResponseDto deactivate(
            Long companyId,
            Long candidateId,
            Long noteId
    ) {
        CandidateNote note = getNote(companyId, candidateId, noteId);
        note.deactivate();
        return candidateNoteMapper.toResponseDto(note);
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

    // Notu şirket ve aday sınırı içerisinde aktif kayıtlardan getirir.
    private CandidateNote getNote(Long companyId, Long candidateId, Long noteId) {
        return candidateNoteRepository
                .findByCompanyIdAndCandidateIdAndId(companyId, candidateId, noteId)
                .filter(CandidateNote::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aday notu bulunamadı: " + noteId
                ));
    }
}
