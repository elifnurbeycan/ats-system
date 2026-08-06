package com.yasarbilgi.ats.company.controller;

import com.yasarbilgi.ats.company.dto.request.*;
import com.yasarbilgi.ats.company.dto.response.*;
import com.yasarbilgi.ats.company.service.CompanyService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform/companies")
@RequiredArgsConstructor
public class PlatformCompanyController {

    private final CompanyService companyService;

    // Yeni şirketi ilk kullanıcılarıyla birlikte oluşturur.
    @PostMapping
    public ResponseEntity<ApiResponse<CreatedCompanyResponseDto>> create(
            @Valid @RequestBody CreateCompanyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Şirket ve ilk kullanıcıları oluşturuldu.", companyService.create(request)));
    }

    // Platformdaki tüm şirketleri listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(companyService.getAll()));
    }

    // Kimliği verilen şirketi getirir.
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getById(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getById(companyId)));
    }

    // Şirketin görünen adını günceller.
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> update(
            @PathVariable Long companyId, @Valid @RequestBody UpdateCompanyRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Şirket güncellendi.",
                companyService.update(companyId, request)));
    }

    // Şirketin aktiflik durumunu günceller.
    @PatchMapping("/{companyId}/status")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> changeStatus(
            @PathVariable Long companyId, @Valid @RequestBody ChangeCompanyStatusRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Şirket durumu güncellendi.",
                companyService.changeStatus(companyId, request)));
    }
}
