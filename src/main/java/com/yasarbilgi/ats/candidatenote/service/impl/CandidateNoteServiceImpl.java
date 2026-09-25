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
import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import com.yasarbilgi.ats.pipeline.repository.PipelineStageRepository;
import com.yasarbilgi.ats.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.common.exception.BusinessRuleException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateNoteServiceImpl implements CandidateNoteService {

    private final CandidateRepository candidateRepository;
    private final CandidateProcessRepository candidateProcessRepository;
    private final PipelineStageRepository pipelineStageRepository;
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
                .pipelineStage(resolveStage(companyId, process, request.pipelineStageId()))
                .entryType("NOTE")
                .content(request.content().trim())
                .build();

        return candidateNoteMapper.toResponseDto(candidateNoteRepository.save(note));
    }

    @Override
    @Transactional
    public CandidateNoteResponseDto createEvaluation(Long companyId, Long candidateId, CreateCandidateNoteRequestDto request) {
        Candidate candidate = getCandidate(companyId, candidateId);
        CandidateProcess process = getCandidateProcess(companyId, candidateId, request.candidateProcessId());
        PipelineStage stage = resolveStage(companyId, process, request.pipelineStageId());
        CandidateNote evaluation = CandidateNote.builder()
                .company(candidate.getCompany()).candidate(candidate).candidateProcess(process)
                .pipelineStage(stage)
                .entryType("EVALUATION").content(request.content().trim()).build();
        return candidateNoteMapper.toResponseDto(candidateNoteRepository.save(evaluation));
    }

    // Adayın genel ve süreç notlarını veya yalnızca seçilen sürecin notlarını getirir.
    @Override
    public PageResponse<CandidateNoteResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId,
            int page,
            int size
    ) {
        getCandidate(companyId, candidateId);

        if (page < 0 || size < 1 || size > 200) throw new BusinessRuleException("Geçersiz sayfalama bilgisi.");
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CandidateNote> notes;
        if (candidateProcessId == null) {
            notes = candidateNoteRepository
                    .findAllByCompanyIdAndCandidateIdAndEntryTypeAndActiveTrue(
                            companyId,
                            candidateId,
                            "NOTE",
                            pageable
                    );
        } else {
            getCandidateProcess(companyId, candidateId, candidateProcessId);
            notes = candidateNoteRepository
                    .findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndEntryTypeAndActiveTrue(
                            companyId,
                            candidateId,
                            candidateProcessId,
                            "NOTE",
                            pageable
                    );
        }

        return PageResponse.from(notes, candidateNoteMapper::toResponseDto);
    }

    @Override
    public PageResponse<CandidateNoteResponseDto> getEvaluations(Long companyId, Long candidateId, Long candidateProcessId, int page, int size) {
        getCandidate(companyId, candidateId);
        if (page < 0 || size < 1 || size > 200) throw new BusinessRuleException("Geçersiz sayfalama bilgisi.");
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CandidateNote> evaluations = candidateProcessId == null
                ? candidateNoteRepository.findAllByCompanyIdAndCandidateIdAndEntryTypeAndActiveTrue(companyId, candidateId, "EVALUATION", pageable)
                : candidateNoteRepository.findAllByCompanyIdAndCandidateIdAndCandidateProcessIdAndEntryTypeAndActiveTrue(companyId, candidateId, candidateProcessId, "EVALUATION", pageable);
        return PageResponse.from(evaluations, candidateNoteMapper::toResponseDto);
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

    @Override
    @Transactional
    public CandidateNoteResponseDto updateEvaluation(Long companyId, Long candidateId, Long evaluationId, UpdateCandidateNoteRequestDto request) {
        CandidateNote evaluation = getNote(companyId, candidateId, evaluationId);
        if (!"EVALUATION".equals(evaluation.getEntryType())) throw new ResourceNotFoundException("Aday değerlendirmesi bulunamadı: " + evaluationId);
        evaluation.updateContent(request.content().trim());
        return candidateNoteMapper.toResponseDto(evaluation);
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

    @Override
    @Transactional
    public CandidateNoteResponseDto deactivateEvaluation(Long companyId, Long candidateId, Long evaluationId) {
        CandidateNote evaluation = getNote(companyId, candidateId, evaluationId);
        if (!"EVALUATION".equals(evaluation.getEntryType())) throw new ResourceNotFoundException("Aday değerlendirmesi bulunamadı: " + evaluationId);
        evaluation.deactivate();
        return candidateNoteMapper.toResponseDto(evaluation);
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

    private PipelineStage resolveStage(Long companyId, CandidateProcess process, Long stageId) {
        if (stageId == null) return null;
        if (process == null) throw new BusinessRuleException("Aşama seçebilmek için aday başvurusu seçilmelidir.");
        return pipelineStageRepository.findByCompanyIdAndPipelineIdAndId(
                        companyId, process.getPipeline().getId(), stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Seçilen işe alım aşaması bulunamadı: " + stageId));
    }
}
