package com.yasarbilgi.ats.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequestDto(
        @NotBlank @Size(max = 150) String name,
        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "yalnızca harf, rakam, alt çizgi ve kısa çizgi içerebilir"
        )
        String code,
        @Size(max = 500) String description
) {
}
