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

    // Oturum kullanıcısının departman yöneticisi rolüne sahip olup olmadığını kontrol eder.
    public boolean hasDepartmentScope() {
        return hasRole("ROLE_DEPARTMENT_MANAGER");
    }

    // Kullanıcının yalnızca atandığı görüşmeler kapsamında çalışıp çalışmadığını kontrol eder.
    public boolean hasInterviewerScope() {
        return !hasCompanyScope() && !hasDepartmentScope() && hasRole("ROLE_INTERVIEWER");
    }

    // JWT içindeki güncel kullanıcı kimliğini getirir.
    public Long getCurrentUserId() {
        Number userId = jwt().getToken().getClaim("userId");
        if (userId == null) throw new ForbiddenException("Oturum kullanıcı kimliği bulunamadı.");
        return userId.longValue();
    }

    // Departman kapsamındaki kullanıcının yönetebildiği departman kimliklerini getirir.
    public Set<Long> getManagedDepartmentIds() {
        if (hasCompanyScope()) return Set.of();
        Set<Long> ids = new HashSet<>();
        Object claim = jwt().getToken().getClaim("managedDepartmentIds");
        if (claim instanceof Collection<?> values) {
            values.stream().filter(Number.class::isInstance).map(Number.class::cast)
                    .map(Number::longValue).forEach(ids::add);
        }
        // Departman yöneticisinin kullanıcı profilindeki ana departmanı her zaman
        // veri kapsamına dahildir. Bu, ayrı yönetici ataması olmayan hesapların
        // kendi departmanındaki pozisyon ve adayları görebilmesini sağlar.
        if (hasDepartmentScope()) {
            Number departmentId = jwt().getToken().getClaim("departmentId");
            if (departmentId != null) ids.add(departmentId.longValue());
        }
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

    // Oturumun belirtilen role sahip olup olmadığını kontrol eder.
    private boolean hasRole(String role) {
        return authentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
