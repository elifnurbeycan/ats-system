package com.yasarbilgi.ats.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Long> securityAuditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken jwt) || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            Number userId = jwt.getToken().getClaim("userId");
            return userId == null ? Optional.empty() : Optional.of(userId.longValue());
        };
    }
}
