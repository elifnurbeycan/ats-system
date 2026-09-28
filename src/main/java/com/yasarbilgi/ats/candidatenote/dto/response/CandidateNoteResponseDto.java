package com.yasarbilgi.ats.candidatenote.dto.response;

import java.time.Instant;

public record CandidateNoteResponseDto(
        Long id,
        Long candidateId,
        Long candidateProcessId,
        Long pipelineStageId,
        String pipelineStageName,
        String entryType,
        String content,
        Long createdBy,
        String createdByName,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
