package com.yasarbilgi.ats.department.dto.response;

import java.time.Instant;

public record DepartmentManagerAssignmentResponseDto(
        Long id,
        Long departmentId,
        String departmentName,
        Long userId,
        String userFullName,
        String userEmail,
        Instant startedAt,
        Instant endedAt,
        boolean active
) {
}
