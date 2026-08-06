package com.yasarbilgi.ats.position.dto.request;

import com.yasarbilgi.ats.position.entity.PositionStatus;
import jakarta.validation.constraints.NotNull;

public record ChangePositionStatusRequestDto(
        @NotNull PositionStatus status
) {
}
