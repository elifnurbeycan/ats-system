package com.yasarbilgi.ats.auth.service.impl;

import com.yasarbilgi.ats.auth.dto.response.PlatformAdminResponseDto;
import com.yasarbilgi.ats.auth.entity.PlatformAdmin;
import com.yasarbilgi.ats.auth.repository.PlatformAdminRepository;
import com.yasarbilgi.ats.auth.service.PlatformAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class PlatformAuthServiceImpl implements PlatformAuthService {
    private final PlatformAdminRepository adminRepository;
    // JWT kimliğine göre platform yöneticisi profilini getirir.
    @Override public PlatformAdminResponseDto getCurrentAdmin(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            Optional<PlatformAdmin> localAdmin = adminRepository.findByEmailIgnoreCase(email)
                    .filter(PlatformAdmin::isActive);
            if (localAdmin.isPresent()) {
                PlatformAdmin admin = localAdmin.get();
                return new PlatformAdminResponseDto(admin.getId(), admin.getFullName(), admin.getEmail());
            }
        }

        String fullName = jwt.getClaimAsString("name");
        if (fullName == null || fullName.isBlank()) {
            fullName = jwt.getClaimAsString("preferred_username");
        }
        if (fullName == null || fullName.isBlank()) {
            fullName = "Keycloak Admin";
        }
        return new PlatformAdminResponseDto(null, fullName, email);
    }
}
