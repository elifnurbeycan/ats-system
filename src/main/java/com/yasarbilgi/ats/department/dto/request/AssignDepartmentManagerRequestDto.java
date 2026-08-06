package com.yasarbilgi.ats.department.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignDepartmentManagerRequestDto(
        @NotNull Long userId
) {
}
