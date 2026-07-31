package com.yasarbilgi.ats.candidateprocess.dto.response;

public record CandidateCardResponseDto(
        Long candidateProcessId,
        Long candidateId,
        String fullName,
        String linkedinUrl
) {
}
