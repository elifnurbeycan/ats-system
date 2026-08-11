package com.yasarbilgi.ats.department.controller;

import com.yasarbilgi.ats.common.response.ApiResponse;
import com.yasarbilgi.ats.common.response.PageResponse;
import com.yasarbilgi.ats.department.dto.request.CreateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.request.UpdateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.ats.department.service.DepartmentService;
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
@RequestMapping("/api/v1/companies/{companyId}/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    // Şirkete yeni bir departman ekler.
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> create(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateDepartmentRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Departman oluşturuldu.",
                        departmentService.create(companyId, request)
                ));
    }

    // Şirket departmanlarını aktiflik filtresine göre listeler.
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DepartmentResponseDto>>> getAll(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.getAll(companyId, includeInactive, page, size)
        ));
    }

    // Şirkete ait departmanın detayını getirir.
    @GetMapping("/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> getById(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                departmentService.getById(companyId, departmentId)
        ));
    }

    // Departmanın adını ve açıklamasını günceller.
    @PutMapping("/{departmentId}")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> update(
            @PathVariable Long companyId,
            @PathVariable Long departmentId,
            @Valid @RequestBody UpdateDepartmentRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Departman güncellendi.",
                departmentService.update(companyId, departmentId, request)
        ));
    }

    // Departmanı fiziksel olarak silmeden pasifleştirir.
    @PatchMapping("/{departmentId}/deactivate")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> deactivate(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Departman silindi.",
                departmentService.deactivate(companyId, departmentId)
        ));
    }

    // Pasif departmanı yeniden aktif hâle getirir.
    @PatchMapping("/{departmentId}/activate")
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> activate(
            @PathVariable Long companyId,
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Departman aktifleştirildi.",
                departmentService.activate(companyId, departmentId)
        ));
    }
}
