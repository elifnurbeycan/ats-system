package com.yasarbilgi.ats.candidateprocess.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeCandidateStageRequestDto(
        @NotNull Long stageId,
        @Size(max = 500) String reason
) {
}
