package com.yasarbilgi.ats.candidate.dto.response;

import java.util.List;

public record CandidateDetailResponseDto(
        CandidateResponseDto candidate,
        List<CandidateProcessSummaryResponseDto> processes
) {
}
