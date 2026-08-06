package com.yasarbilgi.ats.activity.dto.response;

import com.yasarbilgi.ats.activity.entity.ActivityType;
import java.time.Instant;

public record ActivityResponseDto(
        ActivityType type,
        Long referenceId,
        Long candidateProcessId,
        String title,
        String description,
        String status,
        Instant occurredAt,
        Instant targetAt,
        Long performedBy
) {}
