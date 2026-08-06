package com.yasarbilgi.ats.auth.dto.request;
import jakarta.validation.constraints.*;
public record PlatformLoginRequestDto(@NotBlank @Email @Size(max = 255) String email,
                                      @NotBlank @Size(max = 200) String password) {}
