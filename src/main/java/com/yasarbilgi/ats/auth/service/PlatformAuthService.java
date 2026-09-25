package com.yasarbilgi.ats.auth.service;
import com.yasarbilgi.ats.auth.dto.response.PlatformAdminResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;
public interface PlatformAuthService {
    // Oturumdaki platform yöneticisini getirir.
    PlatformAdminResponseDto getCurrentAdmin(Jwt jwt);
}
