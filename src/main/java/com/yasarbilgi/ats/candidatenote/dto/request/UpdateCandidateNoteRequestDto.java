package com.yasarbilgi.ats.candidatenote.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCandidateNoteRequestDto(
        @NotBlank @Size(max = 5000) String content
) {
}
