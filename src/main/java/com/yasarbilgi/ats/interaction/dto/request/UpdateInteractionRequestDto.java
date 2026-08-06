package com.yasarbilgi.ats.interaction.dto.request;

import com.yasarbilgi.ats.interaction.entity.InteractionChannel;
import com.yasarbilgi.ats.interaction.entity.InteractionDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateInteractionRequestDto(
        @NotNull InteractionChannel channel,
        @NotNull InteractionDirection direction,
        @NotNull Instant occurredAt,
        @Size(max = 200) String subject,
        @NotBlank @Size(max = 5000) String summary
) {
}
