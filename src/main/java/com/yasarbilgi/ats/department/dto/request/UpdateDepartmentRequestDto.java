package com.yasarbilgi.ats.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequestDto(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description
) {
}
