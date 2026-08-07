package com.yasarbilgi.ats.auth.dto.response;
import java.util.Set;
public record AuthenticatedUserResponseDto(Long id, Long companyId, String companyCode,
        String fullName, String email, Long departmentId, Set<String> roles,
        Set<String> permissions) {}
