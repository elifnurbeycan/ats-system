package com.yasarbilgi.ats.candidateprocess.dto.response;

public record CandidateProcessResponseDto(
        Long id,
        Long candidateId,
        String candidateFullName,
        Long positionId,
        Long pipelineId,
        Long currentStageId,
        String currentStageName
) {
}
