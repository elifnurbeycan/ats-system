package com.yasarbilgi.ats.auth.service;
import com.yasarbilgi.ats.auth.dto.response.AuthenticatedUserResponseDto;
import org.springframework.security.oauth2.jwt.Jwt;
public interface AuthService {
    // JWT içindeki kimliğe göre oturumdaki kullanıcıyı getirir.
    AuthenticatedUserResponseDto getCurrentUser(Jwt jwt);
}
