package com.yasarbilgi.ats.auth.service.impl;

import com.yasarbilgi.ats.auth.dto.request.*;
import com.yasarbilgi.ats.auth.dto.response.*;
import com.yasarbilgi.ats.auth.entity.RefreshToken;
import com.yasarbilgi.ats.auth.repository.RefreshTokenRepository;
import com.yasarbilgi.ats.auth.service.AuthService;
import com.yasarbilgi.ats.common.exception.*;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.security.config.JwtProperties;
import com.yasarbilgi.ats.user.entity.*;
import com.yasarbilgi.ats.user.repository.UserRepository;
import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
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
public class AuthServiceImpl implements AuthService {
    private static final String TOKEN_TYPE = "Bearer";
    private static final String DEPARTMENT_MANAGER_ROLE_CODE = "DEPARTMENT_MANAGER";
    private final UserRepository userRepository;
    private final DepartmentManagerAssignmentRepository managerAssignmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    // Kullanıcı bilgilerini doğrular ve access/refresh token çifti oluşturur.
    @Override @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByCompanyCodeIgnoreCaseAndEmailIgnoreCase(
                        request.companyCode().trim().toLowerCase(Locale.ROOT), request.email().trim())
                .orElseThrow(this::invalidCredentials);
        validateLoginUser(user);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw invalidCredentials();
        return issueTokenPair(user);
    }

    // Refresh tokenı tek kullanımlık olacak şekilde döndürür ve yeni token çifti verir.
    @Override @Transactional
    public TokenResponseDto refresh(RefreshTokenRequestDto request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Refresh token geçersiz."));
        if (!stored.isUsable(Instant.now())) throw new UnauthorizedException("Refresh token geçersiz veya süresi dolmuş.");
        validateLoginUser(stored.getUser());
        stored.revoke();
        return issueTokenPair(stored.getUser());
    }

    // Gönderilen refresh token bulunuyorsa iptal eder.
    @Override @Transactional
    public void logout(RefreshTokenRequestDto request) {
        refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .ifPresent(RefreshToken::revoke);
    }

    // Access token kimliklerini kullanarak güncel kullanıcı profilini getirir.
    @Override
    public AuthenticatedUserResponseDto getCurrentUser(Jwt jwt) {
        Long userId = ((Number) jwt.getClaim("userId")).longValue();
        Long companyId = ((Number) jwt.getClaim("companyId")).longValue();
        User user = userRepository.findWithDetailsByCompanyIdAndId(companyId, userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException("Oturum kullanıcısı bulunamadı."));
        return toUserResponse(user);
    }

    // Kullanıcı için imzalı access token ve veritabanında özeti saklanan refresh token üretir.
    private TokenResponseDto issueTokenPair(User user) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plus(Duration.ofMinutes(jwtProperties.accessTokenMinutes()));
        Set<String> roles = user.getRoles().stream().map(Role::getCode)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::isActive)
                .map(permission -> permission.getCode().name())
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> managedDepartmentIds = new HashSet<>(managerAssignmentRepository
                .findAllByCompanyIdAndUserIdAndActiveTrue(user.getCompany().getId(), user.getId())
                .stream().map(assignment -> assignment.getDepartment().getId())
                .collect(java.util.stream.Collectors.toSet()));
        // Kullanıcı yönetim ekranında seçilen ana departman, departman yöneticisinin
        // doğal veri kapsamıdır. Ayrı yönetici atamaları varsa bunlarla birleştirilir.
        if (roles.contains(DEPARTMENT_MANAGER_ROLE_CODE) && user.getDepartment() != null) {
            managedDepartmentIds.add(user.getDepartment().getId());
        }
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().issuer(jwtProperties.issuer())
                .issuedAt(now).expiresAt(accessExpiry).subject(user.getId().toString())
                .claim("userId", user.getId()).claim("companyId", user.getCompany().getId())
                .claim("managedDepartmentIds", managedDepartmentIds)
                .claim("roles", roles).claim("permissions", permissions);
        if (user.getDepartment() != null) {
            claimsBuilder.claim("departmentId", user.getDepartment().getId());
        }
        JwtClaimsSet claims = claimsBuilder.build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        String rawRefreshToken = generateOpaqueToken();
        refreshTokenRepository.save(RefreshToken.builder().company(user.getCompany()).user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(now.plus(Duration.ofDays(jwtProperties.refreshTokenDays()))).build());
        return new TokenResponseDto(accessToken, rawRefreshToken, TOKEN_TYPE,
                Duration.between(now, accessExpiry).toSeconds());
    }

    // Kullanıcının ve şirketinin giriş yapmaya uygun durumda olduğunu doğrular.
    private void validateLoginUser(User user) {
        if (!user.isActive() || !user.getCompany().isActive()
                || user.getCompany().getStatus() != CompanyStatus.ACTIVE || user.getStatus() != UserStatus.ACTIVE
                || user.getPasswordHash() == null) throw invalidCredentials();
    }
    // Kullanıcı entity'sini oturum profili yanıtına dönüştürür.
    private AuthenticatedUserResponseDto toUserResponse(User user) {
        return new AuthenticatedUserResponseDto(user.getId(), user.getCompany().getId(),
                user.getCompany().getCode(), user.getFullName(), user.getEmail(),
                user.getDepartment() == null ? null : user.getDepartment().getId(),
                user.getRoles().stream().map(Role::getCode)
                        .collect(java.util.stream.Collectors.toSet()));
    }
    // Kriptografik olarak rastgele ve URL güvenli refresh token üretir.
    private String generateOpaqueToken() { byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    // Ham refresh tokenı veritabanında saklanmayacak SHA-256 özetine dönüştürür.
    private String hash(String token) { try { return HexFormat.of().formatHex(
            MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); } }
    // Kullanıcı bilgisinin bulunup bulunmadığını belli etmeyen ortak giriş hatasını üretir.
    private UnauthorizedException invalidCredentials() { return new UnauthorizedException("Şirket kodu, e-posta veya şifre hatalı."); }
}
