package com.yasarbilgi.ats.candidate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCandidateRequestDto(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 500) String linkedinUrl,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @Size(max = 100) String city,
        @Size(max = 150) String currentCompany,
        @Size(max = 150) String currentJobTitle,
        @Min(0) Integer noticePeriodDays
) {
}
