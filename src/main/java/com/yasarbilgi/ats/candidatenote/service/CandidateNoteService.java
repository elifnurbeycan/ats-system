package com.yasarbilgi.ats.candidatenote.service;

import com.yasarbilgi.ats.candidatenote.dto.request.CreateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.request.UpdateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.response.CandidateNoteResponseDto;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

public interface CandidateNoteService {

    // Adaya genel veya belirli bir sürece bağlı not ekler.
    CandidateNoteResponseDto create(
            Long companyId,
            Long candidateId,
            CreateCandidateNoteRequestDto request
    );

    // Aday notlarını isteğe bağlı süreç filtresiyle listeler.
    PageResponse<CandidateNoteResponseDto> getAll(
            Long companyId,
            Long candidateId,
            Long candidateProcessId,
            int page,
            int size
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
