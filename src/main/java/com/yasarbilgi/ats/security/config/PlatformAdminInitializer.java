package com.yasarbilgi.ats.security.config;

import com.yasarbilgi.ats.auth.entity.PlatformAdmin;
import com.yasarbilgi.ats.auth.repository.PlatformAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor
public class PlatformAdminInitializer implements ApplicationRunner {
    private final PlatformAdminProperties properties;
    private final PlatformAdminRepository repository;
    private final PasswordEncoder passwordEncoder;
    // Ortam değişkenleri verilmişse ilk platform yöneticisini yalnızca bir kez oluşturur.
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (properties.email() == null || properties.email().isBlank()) return;
        if (properties.password() == null || properties.password().length() < 12)
            throw new IllegalStateException("PLATFORM_ADMIN_PASSWORD en az 12 karakter olmalıdır.");
        repository.findByEmailIgnoreCase(properties.email()).orElseGet(() -> repository.save(
                PlatformAdmin.builder().fullName(properties.fullName() == null ? "Super Admin" : properties.fullName().trim())
                        .email(properties.email().trim().toLowerCase(java.util.Locale.ROOT))
                        .passwordHash(passwordEncoder.encode(properties.password())).build()));
    }
}
