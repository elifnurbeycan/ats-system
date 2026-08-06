package com.yasarbilgi.ats.company.dto.request;
import jakarta.validation.constraints.*;
public record InitialCompanyUserRequestDto(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 200) String temporaryPassword) {}
