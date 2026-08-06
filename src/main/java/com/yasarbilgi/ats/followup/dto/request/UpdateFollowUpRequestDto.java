package com.yasarbilgi.ats.followup.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record UpdateFollowUpRequestDto(
        @NotNull Long assignedToUserId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull Instant dueAt
) {}
