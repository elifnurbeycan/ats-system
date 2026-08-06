package com.yasarbilgi.ats.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
public record RefreshTokenRequestDto(@NotBlank String refreshToken) {}
