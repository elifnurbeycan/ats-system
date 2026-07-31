package com.yasarbilgi.ats.position.dto.response;

import com.yasarbilgi.ats.position.entity.PositionStatus;

public record PositionSummaryResponseDto(
        Long id,
        String title,
        String code,
        PositionStatus status
) {
}
