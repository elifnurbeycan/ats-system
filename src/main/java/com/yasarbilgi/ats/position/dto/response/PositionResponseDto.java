package com.yasarbilgi.ats.position.dto.response;

import com.yasarbilgi.ats.position.entity.PositionStatus;

import java.time.Instant;

public record PositionResponseDto(
        Long id,
        Long departmentId,
        String departmentName,
        String title,
        String code,
        String description,
        Integer vacancyCount,
        PositionStatus status,
        Instant openedAt,
        Instant closedAt,
        boolean active
) {
}
