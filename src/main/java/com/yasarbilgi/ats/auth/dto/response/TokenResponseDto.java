package com.yasarbilgi.ats.auth.dto.response;
public record TokenResponseDto(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}
