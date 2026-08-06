package com.yasarbilgi.ats.department.service;

import com.yasarbilgi.ats.department.dto.request.CreateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.request.UpdateDepartmentRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    // Şirkete yeni bir departman oluşturur.
    DepartmentResponseDto create(Long companyId, CreateDepartmentRequestDto request);

    // Şirket departmanlarını aktiflik tercihine göre listeler.
    List<DepartmentResponseDto> getAll(Long companyId, boolean includeInactive);

    // Şirkete ait departmanın detayını getirir.
    DepartmentResponseDto getById(Long companyId, Long departmentId);

    // Departmanın görünen adı ve açıklamasını günceller.
    DepartmentResponseDto update(
            Long companyId,
            Long departmentId,
            UpdateDepartmentRequestDto request
    );

    // Departmanı fiziksel olarak silmeden pasifleştirir.
    DepartmentResponseDto deactivate(Long companyId, Long departmentId);

    // Pasif departmanı yeniden aktif hâle getirir.
    DepartmentResponseDto activate(Long companyId, Long departmentId);
}
