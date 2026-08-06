package com.yasarbilgi.ats.candidateprocess.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record UpdateCandidateCompensationRequestDto(
        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal currentSalary,

        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal expectedSalary,

        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 17, fraction = 2)
        BigDecimal offeredSalary,

        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "üç harfli para birimi kodu olmalıdır"
        )
        String salaryCurrency
) {
}
