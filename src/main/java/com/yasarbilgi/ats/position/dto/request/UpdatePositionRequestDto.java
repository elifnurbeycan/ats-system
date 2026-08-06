package com.yasarbilgi.ats.position.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePositionRequestDto(
        @NotNull Long departmentId,
        @NotBlank @Size(max = 150) String title,
        String description,
        @NotNull @Min(1) Integer vacancyCount
) {
}
