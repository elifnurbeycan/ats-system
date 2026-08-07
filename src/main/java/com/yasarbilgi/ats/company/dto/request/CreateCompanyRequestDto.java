package com.yasarbilgi.ats.company.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record CreateCompanyRequestDto(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String code,
        @NotNull @Valid InitialCompanyUserRequestDto companyAdmin) {}
