package com.yasarbilgi.ats.company.dto.request;
import jakarta.validation.constraints.*;
public record UpdateCompanyRequestDto(@NotBlank @Size(max = 150) String name) {}
