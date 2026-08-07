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
import jakarta.servlet.http.HttpServletRequest;
import com.yasarbilgi.ats.common.ratelimit.service.LoginAttemptService;
import com.yasarbilgi.ats.common.ratelimit.service.ClientIpResolver;
import com.yasarbilgi.ats.common.exception.UnauthorizedException;

@RestController @RequiredArgsConstructor @RequestMapping("/api/v1/auth/platform")
public class PlatformAuthController {
    private final PlatformAuthService service;
    private final LoginAttemptService loginAttemptService;
    private final ClientIpResolver clientIpResolver;
    // Platform yöneticisi oturumunu açar.
    @PostMapping("/login") public ResponseEntity<ApiResponse<TokenResponseDto>> login(
            @Valid @RequestBody PlatformLoginRequestDto request, HttpServletRequest httpRequest) {
        String key = "platform:" + request.email();
        String endpoint = httpRequest.getRequestURI();
        String clientIp = clientIpResolver.resolve(httpRequest);
        loginAttemptService.checkAllowed(clientIp, key, endpoint);
        try {
            TokenResponseDto tokens = service.login(request);
            loginAttemptService.recordSuccess(key);
            return ResponseEntity.ok(ApiResponse.success(tokens));
        } catch (UnauthorizedException exception) {
            loginAttemptService.recordFailure(clientIp, key, endpoint);
            throw exception;
        }
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
