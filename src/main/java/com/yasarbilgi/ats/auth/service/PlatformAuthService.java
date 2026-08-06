package com.yasarbilgi.ats.auth.service;
import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import org.springframework.security.oauth2.jwt.Jwt;
public interface PlatformAuthService {
    // Platform yöneticisinin oturumunu açar.
    TokenResponseDto login(PlatformLoginRequestDto request);
    // Platform refresh tokenını döndürüp yeni token çifti üretir.
    TokenResponseDto refresh(RefreshTokenRequestDto request);
    // Platform refresh tokenını iptal eder.
    void logout(RefreshTokenRequestDto request);
    // Oturumdaki platform yöneticisini getirir.
    PlatformAdminResponseDto getCurrentAdmin(Jwt jwt);
}
