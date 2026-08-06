package com.yasarbilgi.ats.security.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, PlatformAdminProperties.class})
public class SecurityConfig {
    // API'yi stateless JWT doğrulamasıyla çalıştırır; iş endpointleri sonraki adımda korunacaktır.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/platform/**", "/api/v1/auth/platform/me").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(resource -> resource.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }
    // Kullanıcı şifrelerini BCrypt ile doğrulayan encoder'ı oluşturur.
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    // JWT roles claim değerlerini Spring Security ROLE_ yetkilerine dönüştürür.
    @Bean public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
    // Access token imzalamak için HMAC tabanlı JWT encoder oluşturur.
    @Bean public JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }
    // Gelen access tokenları imza, süre ve issuer bilgisiyle doğrular.
    @Bean public JwtDecoder jwtDecoder(JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(properties)).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
    // Yapılandırmadaki en az 32 karakterli sırrı HMAC anahtarına dönüştürür.
    private SecretKey secretKey(JwtProperties properties) {
        if (properties.secret() == null || properties.secret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET en az 32 karakter olmalıdır.");
        }
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
