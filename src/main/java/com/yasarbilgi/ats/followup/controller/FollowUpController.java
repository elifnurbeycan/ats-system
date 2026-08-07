package com.yasarbilgi.ats.followup.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.followup.dto.request.*;
import com.yasarbilgi.ats.followup.dto.response.FollowUpResponseDto;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import com.yasarbilgi.ats.followup.service.FollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/companies/{companyId}/candidates/{candidateId}/follow-ups")
public class FollowUpController {
    private final FollowUpService service;
    // Aday için yeni takip görevi oluşturur.
    @PostMapping public ResponseEntity<ApiResponse<FollowUpResponseDto>> create(
            @PathVariable Long companyId, @PathVariable Long candidateId,
            @Valid @RequestBody CreateFollowUpRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Takip görevi oluşturuldu.",
                service.create(companyId, candidateId, request)));
    }
    // Aday görevlerini durum ve sorumlu filtreleriyle listeler.
    @GetMapping public ResponseEntity<ApiResponse<PageResponse<FollowUpResponseDto>>> getAll(
            @PathVariable Long companyId, @PathVariable Long candidateId,
            @RequestParam(required = false) FollowUpStatus status,
            @RequestParam(required = false) Long assignedToUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.getAll(
                companyId, candidateId, status, assignedToUserId, page, size)));
    }
    // Bekleyen takip görevinin ayrıntılarını günceller.
    @PutMapping("/{followUpId}") public ResponseEntity<ApiResponse<FollowUpResponseDto>> update(
            @PathVariable Long companyId, @PathVariable Long candidateId, @PathVariable Long followUpId,
            @Valid @RequestBody UpdateFollowUpRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Takip görevi güncellendi.",
                service.update(companyId, candidateId, followUpId, request)));
    }
    // Takip görevini tamamlar veya iptal eder.
    @PatchMapping("/{followUpId}/status") public ResponseEntity<ApiResponse<FollowUpResponseDto>> changeStatus(
            @PathVariable Long companyId, @PathVariable Long candidateId, @PathVariable Long followUpId,
            @Valid @RequestBody ChangeFollowUpStatusRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Takip görevi durumu güncellendi.",
                service.changeStatus(companyId, candidateId, followUpId, request)));
    }
}
