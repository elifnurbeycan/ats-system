package com.yasarbilgi.ats.department.service;

import com.yasarbilgi.ats.department.dto.request.AssignDepartmentManagerRequestDto;
import com.yasarbilgi.ats.department.dto.response.DepartmentManagerAssignmentResponseDto;

import java.util.List;

public interface DepartmentManagerAssignmentService {

    // Departmana uygun role sahip aktif kullanıcıyı yönetici olarak atar.
    DepartmentManagerAssignmentResponseDto assign(
            Long companyId,
            Long departmentId,
            AssignDepartmentManagerRequestDto request
    );

    // Departmanın yönetici atamalarını geçmiş tercihiyle listeler.
    List<DepartmentManagerAssignmentResponseDto> getAll(
            Long companyId,
            Long departmentId,
            boolean includeHistory
    );

    // Aktif departman yöneticisi atamasını tarihçeyi koruyarak sona erdirir.
    DepartmentManagerAssignmentResponseDto endAssignment(
            Long companyId,
            Long departmentId,
            Long assignmentId
    );
}
