package com.yasarbilgi.ats.security.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "security.platform-admin")
public record PlatformAdminProperties(String fullName, String email, String password) {}
