package com.yasarbilgi.ats.candidate.dto.response;

public record CandidateResponseDto(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String linkedinUrl,
        String email,
        String phone,
        String city,
        String currentCompany,
        String currentJobTitle,
        Integer noticePeriodDays,
        boolean active
) {
}
