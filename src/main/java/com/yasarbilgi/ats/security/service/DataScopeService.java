package com.yasarbilgi.ats.security.service;

import com.yasarbilgi.ats.common.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataScopeService {

    private static final Set<String> COMPANY_ROLES = Set.of(
            "ROLE_HR", "ROLE_COMPANY_ADMIN", "ROLE_GENERAL_MANAGER", "ROLE_SUPER_ADMIN");

    // Oturum kullanıcısının şirket genelinde veri görme yetkisi olup olmadığını kontrol eder.
    public boolean hasCompanyScope() {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> COMPANY_ROLES.contains(authority.getAuthority()));
    }

    // Departman kapsamındaki kullanıcının yönetebildiği departman kimliklerini getirir.
    public Set<Long> getManagedDepartmentIds() {
        if (hasCompanyScope()) return Set.of();
        Object claim = jwt().getToken().getClaim("managedDepartmentIds");
        if (!(claim instanceof Collection<?> values)) return Set.of();
        Set<Long> ids = new HashSet<>();
        values.stream().filter(Number.class::isInstance).map(Number.class::cast)
                .map(Number::longValue).forEach(ids::add);
        return ids;
    }

    // Kullanıcının belirtilen departmana erişimini doğrular.
    public void requireDepartmentAccess(Long departmentId) {
        if (!hasCompanyScope() && !getManagedDepartmentIds().contains(departmentId)) {
            throw new ForbiddenException("Bu departmana ait verilere erişim yetkiniz bulunmuyor.");
        }
    }

    // Güncel JWT authentication nesnesini getirir.
    private JwtAuthenticationToken jwt() {
        Authentication authentication = authentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) return jwtAuthentication;
        throw new ForbiddenException("Veri kapsamı belirlenemedi.");
    }

    // Security context içindeki doğrulanmış oturumu getirir.
    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Veri kapsamı belirlenemedi.");
        }
        return authentication;
    }
}
