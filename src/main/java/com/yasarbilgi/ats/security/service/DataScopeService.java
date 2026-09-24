package com.yasarbilgi.ats.security.service;

import com.yasarbilgi.ats.common.exception.ForbiddenException;
import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
import com.yasarbilgi.ats.role.entity.DataScope;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final UserRepository userRepository;
    private final DepartmentManagerAssignmentRepository managerAssignmentRepository;

    private static final Set<String> COMPANY_ROLES = Set.of(
            "ROLE_HR", "ROLE_COMPANY_ADMIN", "ROLE_GENERAL_MANAGER", "ROLE_SUPER_ADMIN");

    // Oturum kullanıcısının şirket genelinde veri görme yetkisi olup olmadığını kontrol eder.
    public boolean hasCompanyScope() {
        boolean explicitCompanyRole = authentication().getAuthorities().stream()
                .anyMatch(authority -> COMPANY_ROLES.contains(authority.getAuthority()));
        if (explicitCompanyRole) return true;

        // Özel rollerin veri kapsamı da güvenlik kararına dahil edilmelidir.
        // Aksi durumda arayüzden "Tüm şirket" seçilen İK rolü, departman yöneticisi
        // gibi değerlendirilir ve yönetilen departman ataması olmadığı için boş döner.
        String subject = jwt().getToken().getSubject();
        return subject != null && userRepository.findByKeycloakUserIdAndActiveTrue(subject)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getDataScope() == DataScope.COMPANY))
                .orElse(false);
    }

    // Oturum kullanıcısının departman yöneticisi rolüne sahip olup olmadığını kontrol eder.
    public boolean hasDepartmentScope() {
        if (hasRole("ROLE_DEPARTMENT_MANAGER")) return true;
        return currentUser().map(user -> user.getRoles().stream()
                .anyMatch(role -> role.getDataScope() == DataScope.DEPARTMENT)).orElse(false);
    }

    // Kullanıcının yalnızca atandığı görüşmeler kapsamında çalışıp çalışmadığını kontrol eder.
    public boolean hasInterviewerScope() {
        if (hasCompanyScope() || hasDepartmentScope()) return false;
        return hasRole("ROLE_INTERVIEWER") || currentUser().map(user -> user.getRoles().stream()
                .anyMatch(role -> role.getDataScope() == DataScope.ASSIGNED)).orElse(false);
    }

    // JWT içindeki güncel kullanıcı kimliğini getirir.
    public Long getCurrentUserId() {
        Number userId = jwt().getToken().getClaim("userId");
        if (userId != null) return userId.longValue();
        return currentUser().map(User::getId)
                .orElseThrow(() -> new ForbiddenException("Oturum kullanıcı kimliği bulunamadı."));
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
        // Özel roller dahil DEPARTMENT kapsamındaki kullanıcıların ana departmanı
        // ve açık yönetici görevlendirmeleri erişim kapsamına dahildir.
        if (hasDepartmentScope()) {
            currentUser().ifPresent(user -> {
                if (user.getDepartment() != null && user.getDepartment().isActive()) {
                    ids.add(user.getDepartment().getId());
                }
                managerAssignmentRepository
                        .findAllByCompanyIdAndUserIdAndActiveTrue(user.getCompany().getId(), user.getId())
                        .stream()
                        .filter(assignment -> assignment.getDepartment().isActive())
                        .map(assignment -> assignment.getDepartment().getId())
                        .forEach(ids::add);
            });
        }
        return ids;
    }

    public void requireCompanyScope() {
        if (!hasCompanyScope()) {
            throw new ForbiddenException("Bu işlem yalnızca şirket kapsamındaki kullanıcılar tarafından yapılabilir.");
        }
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

    private Optional<User> currentUser() {
        String subject = jwt().getToken().getSubject();
        if (subject != null && !subject.isBlank()) {
            Optional<User> keycloakUser = userRepository.findByKeycloakUserIdAndActiveTrue(subject);
            if (keycloakUser.isPresent()) return keycloakUser;
        }
        Number userId = jwt().getToken().getClaim("userId");
        Number companyId = jwt().getToken().getClaim("companyId");
        if (userId == null || companyId == null) return Optional.empty();
        return userRepository.findWithDetailsByCompanyIdAndId(companyId.longValue(), userId.longValue())
                .filter(User::isActive);
    }
}
