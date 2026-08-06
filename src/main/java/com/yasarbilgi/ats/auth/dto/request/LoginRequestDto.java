package com.yasarbilgi.ats.auth.dto.request;
import jakarta.validation.constraints.*;
public record LoginRequestDto(@NotBlank @Size(max = 50) String companyCode,
                              @NotBlank @Email @Size(max = 255) String email,
                              @NotBlank @Size(max = 200) String password) {}
