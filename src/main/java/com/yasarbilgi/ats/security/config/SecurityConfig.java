package com.yasarbilgi.ats.security.config;

import com.yasarbilgi.ats.security.converter.JwtAuthoritiesConverter;
import com.yasarbilgi.ats.security.filter.TenantIsolationFilter;
import com.yasarbilgi.ats.security.filter.DepartmentDataScopeFilter;
import com.yasarbilgi.ats.security.handler.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, PlatformAdminProperties.class, KeycloakAdminProperties.class})
public class SecurityConfig {

    // API endpointlerini JWT, tenant ve permission kurallarıyla korur.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TenantIsolationFilter tenantIsolationFilter,
            DepartmentDataScopeFilter departmentDataScopeFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            JwtAuthoritiesConverter authoritiesConverter
    ) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/application-contract").permitAll()
                        .requestMatchers("/api/v1/platform/**", "/api/v1/auth/platform/me")
                        .hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/audit-logs/**")
                        .hasAuthority("AUDIT_VIEW")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/contact-leads/**")
                        .hasAuthority("CONTACT_LEAD_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/contact-leads")
                        .hasAuthority("CONTACT_LEAD_CREATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/contact-leads/*/resolve")
                        .hasAuthority("CONTACT_LEAD_RESOLVE")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/companies/*/candidate-processes/*/compensation")
                        .hasAuthority("CANDIDATE_COMPENSATION_VIEW")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/companies/*/candidate-processes/*/compensation")
                        .hasAuthority("CANDIDATE_COMPENSATION_UPDATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/users/**",
                                "/api/v1/companies/*/roles").hasAuthority("USER_VIEW")
                        .requestMatchers("/api/v1/companies/*/roles/**")
                        .hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/roles")
                        .hasRole("COMPANY_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/users")
                        .hasAuthority("USER_CREATE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/users/*/reset-password")
                        .hasAuthority("USER_UPDATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/users/*/roles")
                        .hasAuthority("USER_ROLE_ASSIGN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/users/*")
                        .hasAuthority("USER_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/users/**")
                        .hasAuthority("USER_DEACTIVATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/departments/**")
                        .hasAuthority("DEPARTMENT_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/departments/*/managers")
                        .hasAuthority("USER_ROLE_ASSIGN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/departments/*/managers/**")
                        .hasAuthority("USER_ROLE_ASSIGN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/departments")
                        .hasAuthority("DEPARTMENT_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/departments/*")
                        .hasAuthority("DEPARTMENT_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/departments/**")
                        .hasAuthority("DEPARTMENT_DEACTIVATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/positions/**")
                        .hasAuthority("POSITION_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/positions")
                        .hasAuthority("POSITION_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/positions/*")
                        .hasAuthority("POSITION_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/positions/*/status")
                        .hasAuthority("POSITION_STATUS_CHANGE")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/companies/*/pipelines/*/positions/*/board")
                        .hasAuthority("CANDIDATE_PROCESS_VIEW")
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/pipelines/**")
                        .hasAuthority("PIPELINE_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/pipelines/**")
                        .hasAuthority("PIPELINE_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/pipelines/**")
                        .hasAuthority("PIPELINE_MANAGE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/pipelines/**")
                        .hasAuthority("PIPELINE_MANAGE")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/companies/*/candidate-processes/*/interviews/**")
                        .hasAuthority("INTERVIEW_VIEW")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/companies/*/candidate-processes/*/interviews")
                        .hasAuthority("INTERVIEW_CREATE")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/companies/*/candidate-processes/*/interviews/*/evaluations")
                        .hasAuthority("INTERVIEW_EVALUATE")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/companies/*/candidate-processes/*/interviews/**")
                        .hasAuthority("INTERVIEW_CREATE")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/companies/*/candidate-processes/*/interviews/**")
                        .hasAuthority("INTERVIEW_CREATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/candidate-processes/**")
                        .hasAuthority("CANDIDATE_PROCESS_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/candidate-processes")
                        .hasAuthority("CANDIDATE_PROCESS_CREATE")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/companies/*/candidate-processes/*/stage")
                        .hasAuthority("CANDIDATE_STAGE_CHANGE")

                        // Notlar ve değerlendirmeler genel aday matcher'ından önce tanımlanır;
                        // böylece kendi yetkileri olmadan bu alt kaynaklara erişilemez.
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/candidates/*/notes")
                        .hasAuthority("CANDIDATE_NOTE_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/candidates/*/notes")
                        .hasAuthority("CANDIDATE_NOTE_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/candidates/*/notes/*")
                        .hasAuthority("CANDIDATE_NOTE_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/candidates/*/notes/*")
                        .hasAuthority("CANDIDATE_NOTE_UPDATE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/candidates/*/notes/evaluations")
                        .hasAuthority("CANDIDATE_EVALUATION_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/candidates/*/notes/evaluations")
                        .hasAuthority("CANDIDATE_EVALUATION_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/candidates/*/notes/evaluations/*")
                        .hasAuthority("CANDIDATE_EVALUATION_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/candidates/*/notes/evaluations/*")
                        .hasAuthority("CANDIDATE_EVALUATION_UPDATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/candidates", "/api/v1/companies/*/candidates/**")
                        .hasAuthority("CANDIDATE_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/candidates")
                        .hasAuthority("CANDIDATE_CREATE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/companies/*/candidates/**")
                        .hasAuthority("CANDIDATE_UPDATE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/companies/*/candidates/**")
                        .hasAuthority("CANDIDATE_UPDATE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/companies/*/candidates/**")
                        .hasAuthority("CANDIDATE_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/companies/*/candidates/**")
                        .hasAuthority("CANDIDATE_UPDATE")

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/dashboard")
                        .hasAuthority("CANDIDATE_PROCESS_VIEW")
                        .requestMatchers("/api/v1/companies/**").denyAll()
                        .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter(authoritiesConverter)))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterAfter(tenantIsolationFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(departmentDataScopeFilter, TenantIsolationFilter.class)
                .build();
    }

    // JWT rol ve permission claim değerlerini authentication nesnesine aktarır.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(JwtAuthoritiesConverter authoritiesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        String issuer = properties.keycloakIssuer();
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("KEYCLOAK_ISSUER boş bırakılamaz.");
        }
        String audience = properties.keycloakAudience();
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException("KEYCLOAK_AUDIENCE boş bırakılamaz.");
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(
                issuer.replaceAll("/+$", "") + "/protocol/openid-connect/certs").build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                "aud", audiences -> audiences != null && audiences.contains(audience));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return decoder;
    }
}
