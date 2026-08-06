package com.yasarbilgi.ats.auth.service;
import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import org.springframework.security.oauth2.jwt.Jwt;
public interface AuthService {
    // Şirket kodu, e-posta ve şifreyle kullanıcı oturumu açar.
    TokenResponseDto login(LoginRequestDto request);
    // Geçerli refresh tokenı döndürerek yeni token çifti üretir.
    TokenResponseDto refresh(RefreshTokenRequestDto request);
    // Refresh tokenı iptal ederek oturumu sonlandırır.
    void logout(RefreshTokenRequestDto request);
    // JWT içindeki kimliğe göre oturumdaki kullanıcıyı getirir.
    AuthenticatedUserResponseDto getCurrentUser(Jwt jwt);
}
