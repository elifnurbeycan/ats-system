package com.yasarbilgi.ats.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Long> securityAuditorAware() {
        // JPA flush sırasında kullanıcı sorgusu yeniden flush tetikleyebilir.
        // Kimlikli değişikliklerin aktörü AuditMutationAspect içinde Keycloak subject'inden çözülür.
        return Optional::empty;
    }
}
