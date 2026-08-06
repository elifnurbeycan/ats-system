package com.yasarbilgi.ats.candidate.controller;

import com.yasarbilgi.ats.candidate.dto.request.UpdateCandidateRequestDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateDetailResponseDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateResponseDto;
import com.yasarbilgi.ats.candidate.service.CandidateService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    // Aktif adayları arama ve sayfalama seçenekleriyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CandidateResponseDto>>> getAll(
            @PathVariable Long companyId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                candidateService.getAll(companyId, search, page, size)
        ));
    }

    // Aday profilini bağlı işe alım süreçleriyle birlikte getirir.
    @GetMapping("/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateDetailResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long candidateId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                candidateService.getById(companyId, candidateId)
        ));
    }

    // Adayın zorunlu ve isteğe bağlı profil bilgilerini günceller.
    @PutMapping("/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateDetailResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @Valid @RequestBody UpdateCandidateRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Aday bilgileri güncellendi.",
                candidateService.update(companyId, candidateId, request)
        ));
    }
}
