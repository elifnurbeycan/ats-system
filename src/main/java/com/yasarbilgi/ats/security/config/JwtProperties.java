package com.yasarbilgi.ats.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String keycloakIssuer,
        String keycloakAudience,
        boolean keycloakRequired
) {}
