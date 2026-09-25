package com.yasarbilgi.ats.security;

import com.yasarbilgi.ats.security.converter.JwtAuthoritiesConverter;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.permission.entity.PermissionCategory;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.role.entity.DataScope;
import com.yasarbilgi.ats.role.entity.Role;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthoritiesConverterTest {
    private final UserRepository users = mock(UserRepository.class);
    private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter(users);

    @Test
    void unprovisionedRealmRoleCannotGrantTenantPermissions() {
        when(users.findByKeycloakUserIdAndActiveTrue("unknown")).thenReturn(Optional.empty());
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject("unknown")
                .claim("realm_access", Map.of("roles", List.of("COMPANY_ADMIN")))
                .claim("permissions", List.of("CANDIDATE_VIEW")).build();

        assertThat(converter.convert(token)).isEmpty();
    }

    @Test
    void platformRoleIsGrantedOnlyFromKeycloakRealmRole() {
        when(users.findByKeycloakUserIdAndActiveTrue("platform")).thenReturn(Optional.empty());
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject("platform")
                .claim("realm_access", Map.of("roles", List.of("SUPER_ADMIN"))).build();

        assertThat(converter.convert(token)).extracting("authority").containsExactly("ROLE_SUPER_ADMIN");
    }

    @Test
    void suspendedCompanyCannotUseItsLocalUserRoles() {
        Company company = Company.builder().name("Suspended").code("suspended")
                .status(CompanyStatus.SUSPENDED).build();
        User user = User.builder().company(company).firstName("Test").lastName("User")
                .email("test@example.test").keycloakUserId("suspended-user").build();
        when(users.findByKeycloakUserIdAndActiveTrue("suspended-user")).thenReturn(Optional.of(user));
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject("suspended-user")
                .claim("realm_access", Map.of("roles", List.of("SUPER_ADMIN"))).build();

        assertThat(converter.convert(token)).isEmpty();
    }

    @Test
    void activeCompanyAdminGetsRoleAndFullPermissionSetFromLocalUser() {
        Company company = Company.builder().name("Active").code("active")
                .status(CompanyStatus.ACTIVE).build();
        Permission permission = Permission.builder().code(PermissionCode.CANDIDATE_VIEW)
                .name("Aday görüntüleme").category(PermissionCategory.CANDIDATE)
                .displayOrder(1).active(true).build();
        Role admin = Role.builder().code("COMPANY_ADMIN").name("Şirket yöneticisi")
                .dataScope(DataScope.COMPANY).permissions(Set.of(permission)).build();
        User user = User.builder().company(company).firstName("Active").lastName("Admin")
                .email("active@example.test").keycloakUserId("active-admin")
                .roles(Set.of(admin)).build();
        when(users.findByKeycloakUserIdAndActiveTrue("active-admin")).thenReturn(Optional.of(user));

        var authorities = converter.convert(Jwt.withTokenValue("test").header("alg", "none")
                .subject("active-admin").build());

        assertThat(authorities).extracting("authority")
                .contains("ROLE_COMPANY_ADMIN", "CANDIDATE_VIEW", "AUDIT_VIEW");
    }

    @Test
    void unknownUserCannotUseNonPlatformRealmRole() {
        when(users.findByKeycloakUserIdAndActiveTrue("unknown")).thenReturn(Optional.empty());
        Jwt token = Jwt.withTokenValue("test").header("alg", "none").subject("unknown")
                .claim("realm_access", Map.of("roles", List.of("HR", "DEPARTMENT_MANAGER"))).build();

        assertThat(converter.convert(token)).isEmpty();
    }

    @Test
    void inactiveLocalPermissionIsNotConverted() {
        Company company = Company.builder().name("Active").code("active")
                .status(CompanyStatus.ACTIVE).build();
        Permission inactive = Permission.builder().code(PermissionCode.CANDIDATE_UPDATE)
                .name("Aday güncelleme").category(PermissionCategory.CANDIDATE)
                .displayOrder(1).active(false).build();
        Role recruiter = Role.builder().code("RECRUITER").name("İK")
                .dataScope(DataScope.COMPANY).permissions(Set.of(inactive)).build();
        User user = User.builder().company(company).firstName("Test").lastName("User")
                .email("test@example.test").keycloakUserId("inactive-permission")
                .roles(Set.of(recruiter)).build();
        when(users.findByKeycloakUserIdAndActiveTrue("inactive-permission")).thenReturn(Optional.of(user));

        assertThat(converter.convert(Jwt.withTokenValue("test").header("alg", "none")
                .subject("inactive-permission").build()))
                .extracting("authority").containsExactly("ROLE_RECRUITER");
    }
}
