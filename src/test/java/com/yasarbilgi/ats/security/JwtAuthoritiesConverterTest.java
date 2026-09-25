package com.yasarbilgi.ats.security;

import com.yasarbilgi.ats.security.converter.JwtAuthoritiesConverter;
import com.yasarbilgi.ats.company.entity.Company;
import com.yasarbilgi.ats.company.entity.CompanyStatus;
import com.yasarbilgi.ats.user.entity.User;
import com.yasarbilgi.ats.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}
