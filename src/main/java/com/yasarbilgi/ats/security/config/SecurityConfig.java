package com.yasarbilgi.ats.security.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
                        .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/companies/*/audit-logs/**")
                        .hasAuthority("AUDIT_VIEW")

                        .requestMatchers("/api/v1/companies/*/contact-leads/**")
                        .hasAnyRole("HR", "RECRUITER")

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

    // Kullanıcı şifrelerini BCrypt ile doğrulayan encoder'ı oluşturur.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // JWT rol ve permission claim değerlerini authentication nesnesine aktarır.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(JwtAuthoritiesConverter authoritiesConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // Access token imzalamak için HMAC tabanlı JWT encoder oluşturur.
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    // Gelen access tokenları imza, süre ve issuer bilgisiyle doğrular.
    @Bean
    public JwtDecoder jwtDecoder(
            JwtProperties properties
    ) {
        NimbusJwtDecoder legacyDecoder = NimbusJwtDecoder.withSecretKey(secretKey(properties)).build();
        legacyDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));

        String keycloakIssuer = properties.keycloakIssuer();
        if (properties.keycloakRequired() && (keycloakIssuer == null || keycloakIssuer.isBlank())) {
            throw new IllegalStateException("KEYCLOAK_ISSUER, Keycloak zorunlu modda boş bırakılamaz.");
        }
        if (properties.keycloakRequired() && properties.keycloakAudience().isBlank()) {
            throw new IllegalStateException("KEYCLOAK_AUDIENCE, Keycloak zorunlu modda boş bırakılamaz.");
        }
        if (keycloakIssuer == null || keycloakIssuer.isBlank()) return legacyDecoder;

        JwtDecoder keycloakDecoder = JwtDecoders.fromIssuerLocation(keycloakIssuer);
        if (!properties.keycloakAudience().isBlank() && keycloakDecoder instanceof NimbusJwtDecoder nimbusDecoder) {
            OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(keycloakIssuer);
            OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                    "aud",
                    audiences -> audiences != null && audiences.contains(properties.keycloakAudience())
            );
            nimbusDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        }
        if (properties.keycloakRequired()) return keycloakDecoder;

        return token -> {
            try {
                return keycloakDecoder.decode(token);
            } catch (JwtException keycloakException) {
                // Geçiş döneminde daha önce üretilen ATS tokenlarını da kabul et.
                return legacyDecoder.decode(token);
            }
        };
    }

    // Yapılandırmadaki en az 32 karakterli sırrı HMAC anahtarına dönüştürür.
    private SecretKey secretKey(JwtProperties properties) {
        if (properties.secret() == null || properties.secret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET en az 32 karakter olmalıdır.");
        }
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
