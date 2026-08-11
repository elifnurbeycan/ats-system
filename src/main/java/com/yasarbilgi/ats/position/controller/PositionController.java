package com.yasarbilgi.ats.position.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.position.dto.request.ChangePositionStatusRequestDto;
import com.yasarbilgi.ats.position.dto.request.CreatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.request.UpdatePositionRequestDto;
import com.yasarbilgi.ats.position.dto.response.PositionResponseDto;
import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import com.yasarbilgi.ats.position.service.PositionService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/positions")
public class PositionController {

    private final PositionService positionService;

    // Şirket için taslak durumda yeni bir pozisyon oluşturur.
    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody CreatePositionRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Pozisyon taslağı oluşturuldu.",
                        positionService.create(companyId, request)
                ));
    }

    // Pozisyonları isteğe bağlı departman ve durum filtresiyle listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PositionResponseDto>>> getAll(
            @PathVariable Long companyId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) PositionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                positionService.getAll(companyId, departmentId, status, page, size)
        ));
    }

    // Aday oluşturma ekranında seçilebilecek açık pozisyonları listeler.
    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<PositionSummaryResponseDto>>> getOpenPositions(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                positionService.getOpenPositions(companyId)
        ));
    }

    // Şirkete ait pozisyonun detayını getirir.
    @GetMapping("/{positionId}")
    public ResponseEntity<ApiResponse<PositionResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long positionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                positionService.getById(companyId, positionId)
        ));
    }

    // Pozisyonun temel bilgilerini günceller.
    @PutMapping("/{positionId}")
    public ResponseEntity<ApiResponse<PositionResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long positionId,
            @Valid @RequestBody UpdatePositionRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pozisyon bilgileri güncellendi.",
                positionService.update(companyId, positionId, request)
        ));
    }

    // Pozisyonu izin verilen yeni iş akışı durumuna geçirir.
    @PatchMapping("/{positionId}/status")
    public ResponseEntity<ApiResponse<PositionResponseDto>> changeStatus(
            @PathVariable Long companyId,
            @PathVariable Long positionId,
            @Valid @RequestBody ChangePositionStatusRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Pozisyon durumu güncellendi.",
                positionService.changeStatus(companyId, positionId, request)
        ));
    }
}
