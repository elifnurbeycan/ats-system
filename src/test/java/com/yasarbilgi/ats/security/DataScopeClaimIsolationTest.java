package com.yasarbilgi.ats.security;

import com.yasarbilgi.ats.common.exception.ForbiddenException;
import com.yasarbilgi.ats.department.repository.DepartmentManagerAssignmentRepository;
import com.yasarbilgi.ats.security.service.DataScopeService;
import com.yasarbilgi.ats.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class DataScopeClaimIsolationTest {
    private final UserRepository users = mock(UserRepository.class);
    private final DepartmentManagerAssignmentRepository assignments = mock(DepartmentManagerAssignmentRepository.class);
    private final DataScopeService scope = new DataScopeService(users, assignments);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userIdAndManagedDepartmentsClaimsDoNotGrantAccessWithoutLocalUser() {
        when(users.findByKeycloakUserIdAndActiveTrue("unknown")).thenReturn(Optional.empty());
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject("unknown")
                .claim("userId", 42L).claim("companyId", 1L)
                .claim("managedDepartmentIds", List.of(99L)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token,
                List.of(new SimpleGrantedAuthority("ROLE_DEPARTMENT_MANAGER"))));

        assertThat(scope.getManagedDepartmentIds()).isEmpty();
        assertThatThrownBy(scope::getCurrentUserId).isInstanceOf(ForbiddenException.class);
    }
}
