package com.yasarbilgi.ats.auth.controller;
import com.yasarbilgi.ats.auth.dto.response.PlatformAdminResponseDto;
import com.yasarbilgi.ats.auth.service.PlatformAuthService;
import com.yasarbilgi.ats.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth/platform")
public class PlatformAuthController {
    private final PlatformAuthService service;
    // Oturumdaki platform yöneticisini getirir.
    @GetMapping("/me") public ResponseEntity<ApiResponse<PlatformAdminResponseDto>> me(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.success(service.getCurrentAdmin(jwt)));
    }
}
