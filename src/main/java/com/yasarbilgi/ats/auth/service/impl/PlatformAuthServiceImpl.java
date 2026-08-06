package com.yasarbilgi.ats.auth.service.impl;

import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import com.yasarbilgi.ats.auth.entity.*;
import com.yasarbilgi.ats.auth.repository.*;
import com.yasarbilgi.ats.auth.service.PlatformAuthService;
import com.yasarbilgi.ats.common.exception.UnauthorizedException;
import com.yasarbilgi.ats.security.config.JwtProperties;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class PlatformAuthServiceImpl implements PlatformAuthService {
    private final PlatformAdminRepository adminRepository;
    private final PlatformRefreshTokenRepository refreshRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    // Platform yöneticisini doğrulayıp token çifti oluşturur.
    @Override @Transactional public TokenResponseDto login(PlatformLoginRequestDto request) {
        PlatformAdmin admin = adminRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(PlatformAdmin::isActive).orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) throw invalidCredentials();
        return issue(admin);
    }
    // Tek kullanımlık platform refresh tokenını yeniler.
    @Override @Transactional public TokenResponseDto refresh(RefreshTokenRequestDto request) {
        PlatformRefreshToken token = refreshRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Refresh token geçersiz."));
        if (!token.isUsable(Instant.now()) || !token.getPlatformAdmin().isActive())
            throw new UnauthorizedException("Refresh token geçersiz veya süresi dolmuş.");
        token.revoke(); return issue(token.getPlatformAdmin());
    }
    // Platform refresh tokenını bulunuyorsa iptal eder.
    @Override @Transactional public void logout(RefreshTokenRequestDto request) {
        refreshRepository.findByTokenHash(hash(request.refreshToken())).ifPresent(PlatformRefreshToken::revoke);
    }
    // JWT kimliğine göre platform yöneticisi profilini getirir.
    @Override public PlatformAdminResponseDto getCurrentAdmin(Jwt jwt) {
        Long id = ((Number) jwt.getClaim("platformAdminId")).longValue();
        PlatformAdmin admin = adminRepository.findById(id).filter(PlatformAdmin::isActive)
                .orElseThrow(() -> new UnauthorizedException("Platform yöneticisi bulunamadı."));
        return new PlatformAdminResponseDto(admin.getId(), admin.getFullName(), admin.getEmail());
    }
    // Platform yöneticisi için access ve refresh token çifti üretir.
    private TokenResponseDto issue(PlatformAdmin admin) {
        Instant now = Instant.now(), expiry = now.plus(Duration.ofMinutes(properties.accessTokenMinutes()));
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(properties.issuer()).issuedAt(now).expiresAt(expiry)
                .subject("platform:" + admin.getId()).claim("platformAdminId", admin.getId())
                .claim("principalType", "PLATFORM_ADMIN").claim("roles", Set.of("SUPER_ADMIN"))
                .claim("permissions", Set.of(PermissionCode.PIPELINE_VIEW.name(),
                        PermissionCode.PIPELINE_MANAGE.name())).build();
        String access = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        String raw = randomToken();
        refreshRepository.save(PlatformRefreshToken.builder().platformAdmin(admin).tokenHash(hash(raw))
                .expiresAt(now.plus(Duration.ofDays(properties.refreshTokenDays()))).build());
        return new TokenResponseDto(access, raw, "Bearer", Duration.between(now, expiry).toSeconds());
    }
    // URL güvenli rastgele refresh token üretir.
    private String randomToken() { byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    // Ham tokenı SHA-256 özeti haline getirir.
    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); } }
    // Ortak platform giriş hatasını üretir.
    private UnauthorizedException invalidCredentials() { return new UnauthorizedException("E-posta veya şifre hatalı."); }
}
