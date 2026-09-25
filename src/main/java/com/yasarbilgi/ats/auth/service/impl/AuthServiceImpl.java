package com.yasarbilgi.ats.auth.service.impl;

import com.yasarbilgi.ats.auth.dto.response.AuthenticatedUserResponseDto;
import com.yasarbilgi.ats.auth.service.AuthService;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.common.exception.UnauthorizedException;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private static final String HR_ROLE_CODE = "HR";
    private final UserRepository userRepository;

    // Access token kimliklerini kullanarak güncel kullanıcı profilini getirir.
    @Override
    public AuthenticatedUserResponseDto getCurrentUser(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) throw new UnauthorizedException("Keycloak kullanıcı kimliği bulunamadı.");
        User user = userRepository.findByKeycloakUserIdAndActiveTrue(subject)
                .orElseThrow(() -> new UnauthorizedException("Keycloak kullanıcısı ATS kullanıcısıyla eşleştirilemedi."));
        if (user.getCompany().getStatus() != CompanyStatus.ACTIVE)
            throw new UnauthorizedException("Şirket hesabı aktif değil.");
        return toUserResponse(user);
    }
    // Kullanıcı entity'sini oturum profili yanıtına dönüştürür.
    private AuthenticatedUserResponseDto toUserResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getCode)
                .collect(java.util.stream.Collectors.toSet());
        return new AuthenticatedUserResponseDto(user.getId(), user.getCompany().getId(),
                user.getCompany().getCode(), user.getFullName(), user.getEmail(),
                user.getDepartment() == null ? null : user.getDepartment().getId(),
                roles, resolvePermissions(user, roles), user.getRoles().stream()
                        .collect(java.util.stream.Collectors.toMap(Role::getCode, Role::getName,
                                (first, second) -> first)));
    }

    private Set<String> resolvePermissions(User user, Set<String> roles) {
        if (roles.contains(HR_ROLE_CODE)) {
            return Arrays.stream(PermissionCode.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .filter(Permission::isActive)
                .map(permission -> permission.getCode().name())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
