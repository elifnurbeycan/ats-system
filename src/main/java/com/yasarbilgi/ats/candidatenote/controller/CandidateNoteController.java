package com.yasarbilgi.ats.candidatenote.controller;

import com.yasarbilgi.ats.candidatenote.dto.request.CreateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.request.UpdateCandidateNoteRequestDto;
import com.yasarbilgi.ats.candidatenote.dto.response.CandidateNoteResponseDto;
import com.yasarbilgi.ats.candidatenote.service.CandidateNoteService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidates/{candidateId}/notes")
public class CandidateNoteController {

    private final CandidateNoteService candidateNoteService;

    // Adaya genel veya belirli sürece bağlı yeni bir not ekler.
    @PostMapping
    public ResponseEntity<ApiResponse<CandidateNoteResponseDto>> create(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @Valid @RequestBody CreateCandidateNoteRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Aday notu oluşturuldu.",
                        candidateNoteService.create(companyId, candidateId, request)
                ));
    }

    // Aday notlarını isteğe bağlı aday süreci filtresiyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CandidateNoteResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @RequestParam(required = false) Long candidateProcessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                candidateNoteService.getAll(companyId, candidateId, candidateProcessId, page, size)
        ));
    }

    // Aday notunun metin içeriğini günceller.
    @PutMapping("/{noteId}")
    public ResponseEntity<ApiResponse<CandidateNoteResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateCandidateNoteRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Aday notu güncellendi.",
                candidateNoteService.update(companyId, candidateId, noteId, request)
        ));
    }

    // Aday notunu fiziksel olarak silmeden pasifleştirir.
    @PatchMapping("/{noteId}/deactivate")
    public ResponseEntity<ApiResponse<CandidateNoteResponseDto>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @PathVariable Long noteId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Aday notu silindi.",
                candidateNoteService.deactivate(companyId, candidateId, noteId)
        ));
    }
}
