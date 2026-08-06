package com.yasarbilgi.ats.auth.controller;

import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import com.yasarbilgi.ats.auth.service.AuthService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    // Kullanıcı bilgilerini doğrulayıp JWT token çifti döndürür.
    @PostMapping("/login") public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }
    // Refresh tokenla yeni access ve refresh token çifti üretir.
    @PostMapping("/refresh") public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }
    // Refresh tokenı iptal ederek kullanıcı oturumunu kapatır.
    @PostMapping("/logout") public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Oturum kapatıldı.", null));
    }
    // Access tokenla oturum açmış kullanıcının profilini getirir.
    @GetMapping("/me") public ResponseEntity<ApiResponse<AuthenticatedUserResponseDto>> me(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(jwt)));
    }
}
