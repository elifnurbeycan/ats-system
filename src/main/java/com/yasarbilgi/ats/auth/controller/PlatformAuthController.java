package com.yasarbilgi.ats.auth.controller;
import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import com.yasarbilgi.ats.auth.service.PlatformAuthService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth/platform")
public class PlatformAuthController {
    private final PlatformAuthService service;
    // Platform yöneticisi oturumunu açar.
    @PostMapping("/login") public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody PlatformLoginRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(service.login(request)));
    }
    // Platform refresh tokenıyla yeni token çifti üretir.
    @PostMapping("/refresh") public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(service.refresh(request)));
    }
    // Platform yöneticisi oturumunu kapatır.
    @PostMapping("/logout") public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        service.logout(request); return ResponseEntity.ok(ApiResponse.success("Oturum kapatıldı.", null));
    }
    // Oturumdaki platform yöneticisini getirir.
    @GetMapping("/me") public ResponseEntity<ApiResponse<PlatformAdminResponseDto>> me(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(service.getCurrentAdmin(jwt)));
    }
}
