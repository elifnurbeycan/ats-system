package com.yasarbilgi.ats.interaction.dto.response;

import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import com.yasarbilgi.ats.interaction.entity.InteractionDirection;

import java.time.Instant;

public record InteractionResponseDto(
        Long id,
        Long candidateId,
        Long candidateProcessId,
        InteractionChannel channel,
        InteractionDirection direction,
        Instant occurredAt,
        String subject,
        String summary,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
