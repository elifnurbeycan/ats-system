package com.yasarbilgi.ats.interaction.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.interaction.dto.request.CreateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.request.UpdateInteractionRequestDto;
import com.yasarbilgi.ats.interaction.dto.response.InteractionResponseDto;
import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import com.yasarbilgi.ats.interaction.service.InteractionService;
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
@RequestMapping("/api/v1/companies/{companyId}/candidates/{candidateId}/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    // Adaya genel veya belirli sürece bağlı iletişim kaydı ekler.
    @PostMapping
    public ResponseEntity<ApiResponse<InteractionResponseDto>> create(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @Valid @RequestBody CreateInteractionRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "İletişim kaydı oluşturuldu.",
                        interactionService.create(companyId, candidateId, request)
                ));
    }

    // Aday iletişimlerini isteğe bağlı süreç ve kanal filtresiyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InteractionResponseDto>>> getAll(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @RequestParam(required = false) Long candidateProcessId,
            @RequestParam(required = false) InteractionChannel channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                interactionService.getAll(
                        companyId,
                        candidateId,
                        candidateProcessId,
                        channel,
                        page,
                        size
                )
        ));
    }

    // İletişim kaydının ayrıntılarını günceller.
    @PutMapping("/{interactionId}")
    public ResponseEntity<ApiResponse<InteractionResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @PathVariable Long interactionId,
            @Valid @RequestBody UpdateInteractionRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "İletişim kaydı güncellendi.",
                interactionService.update(companyId, candidateId, interactionId, request)
        ));
    }

    // İletişim kaydını fiziksel olarak silmeden pasifleştirir.
    @PatchMapping("/{interactionId}/deactivate")
    public ResponseEntity<ApiResponse<InteractionResponseDto>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @PathVariable Long interactionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "İletişim kaydı pasifleştirildi.",
                interactionService.deactivate(companyId, candidateId, interactionId)
        ));
    }
}
