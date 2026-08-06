package com.yasarbilgi.ats.department.dto.response;

public record DepartmentResponseDto(
        Long id,
        String name,
        String code,
        String description,
        boolean active
) {
}
