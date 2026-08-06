package com.yasarbilgi.ats.followup.dto.response;

import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import java.time.Instant;

public record FollowUpResponseDto(
        Long id, Long candidateId, Long candidateProcessId,
        Long assignedToUserId, String assignedToFullName,
        String title, String description, Instant dueAt,
        FollowUpStatus status, Instant completedAt, boolean overdue, boolean active
) {}
