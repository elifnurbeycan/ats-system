package com.yasarbilgi.ats.auth.controller;

import com.yasarbilgi.ats.auth.dto.response.AuthenticatedUserResponseDto;
import com.yasarbilgi.ats.auth.service.AuthService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    // Access tokenla oturum açmış kullanıcının profilini getirir.
    @GetMapping("/me") public ResponseEntity<ApiResponse<AuthenticatedUserResponseDto>> me(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(jwt)));
    }
}
