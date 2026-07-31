package com.yasarbilgi.ats.candidateprocess.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCandidateProcessRequestDto(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 500) String linkedinUrl,
        @NotNull Long positionId,
        @NotNull Long pipelineId
) {
}
