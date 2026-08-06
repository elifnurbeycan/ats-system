package com.yasarbilgi.ats.candidatenote.dto.response;

import java.time.Instant;

public record CandidateNoteResponseDto(
        Long id,
        Long candidateId,
        Long candidateProcessId,
        String content,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
