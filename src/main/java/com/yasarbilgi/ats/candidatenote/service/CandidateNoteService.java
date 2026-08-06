package com.yasarbilgi.ats.candidatenote.service;

import com.yasarbilgi.ats.candidatenote.dto.request.CreateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.request.UpdateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.response.CandidateNoteResponseDto;

import java.util.List;

public interface CandidateNoteService {

    // Adaya genel veya belirli bir sürece bağlı not ekler.
    CandidateNoteResponseDto create(
            Long companyId,
            Long candidateId,
            CreateCandidateNoteRequestDto request
    );

    // Aday notlarını isteğe bağlı süreç filtresiyle listeler.
    List<CandidateNoteResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId
    );

    // Aday notunun metin içeriğini günceller.
    CandidateNoteResponseDto update(
            Long companyId,
            Long candidateId,
            Long noteId,
            UpdateCandidateNoteRequestDto request
    );

    // Aday notunu fiziksel olarak silmeden pasifleştirir.
    CandidateNoteResponseDto deactivate(
            Long companyId,
            Long candidateId,
            Long noteId
    );
}
