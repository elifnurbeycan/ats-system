package com.yasarbilgi.ats.department.service;

import com.yasarbilgi.ats.department.dto.request.AssignDepartmentManagerRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentManagerAssignmentResponseDto;

import java.util.List;
import com.yasarbilgi.ats.common.response.PageResponse;

public interface DepartmentManagerAssignmentService {

    // Departmana uygun role sahip aktif kullanıcıyı yönetici olarak atar.
    DepartmentManagerAssignmentResponseDto assign(
            Long companyId,
            Long departmentId,
            AssignDepartmentManagerRequestDto request
    );

    // Departmanın yönetici atamalarını geçmiş tercihiyle listeler.
    PageResponse<DepartmentManagerAssignmentResponseDto> getAll(
            Long companyId,
            Long departmentId,
            boolean includeHistory,
            int page,
            int size
    );

    // Aktif departman yöneticisi atamasını tarihçeyi koruyarak sona erdirir.
    DepartmentManagerAssignmentResponseDto endAssignment(
            Long companyId,
            Long departmentId,
            Long assignmentId
    );
}
