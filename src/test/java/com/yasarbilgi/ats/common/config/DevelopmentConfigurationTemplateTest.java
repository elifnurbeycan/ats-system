package com.yasarbilgi.ats.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class DevelopmentConfigurationTemplateTest {

    @Test
    void developmentTemplateDefinesRequiredCorsOrigins() throws IOException {
        var propertySources = new YamlPropertySourceLoader()
                .load("application-dev-example", new ClassPathResource("application-dev.example.yaml"));

        Object allowedOrigins = propertySources.stream()
                .map(propertySource -> propertySource.getProperty("cors.allowed-origins"))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);

        assertThat(allowedOrigins)
                .isEqualTo("${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}");
    }
}
