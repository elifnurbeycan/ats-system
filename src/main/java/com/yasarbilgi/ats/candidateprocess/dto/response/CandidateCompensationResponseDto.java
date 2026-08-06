package com.yasarbilgi.ats.candidateprocess.dto.response;

import java.math.BigDecimal;

public record CandidateCompensationResponseDto(
        Long candidateProcessId,
        BigDecimal currentSalary,
        BigDecimal expectedSalary,
        BigDecimal offeredSalary,
        String salaryCurrency
) {
}
