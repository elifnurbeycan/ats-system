package com.yasarbilgi.ats.position.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePositionRequestDto(
        @NotNull Long departmentId,
        @NotBlank @Size(max = 150) String title,
        @NotBlank
        @Size(max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "yalnızca harf, rakam, alt çizgi ve kısa çizgi içerebilir"
        )
        String code,
        String description,
        @NotNull @Min(1) Integer vacancyCount
) {
}
