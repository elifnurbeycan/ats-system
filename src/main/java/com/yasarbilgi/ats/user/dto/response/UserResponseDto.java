package com.yasarbilgi.ats.user.dto.response;

import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.user.entity.UserStatus;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        Long departmentId,
        String departmentName,
        UserStatus status,
        boolean active,
        Set<RoleResponseDto> roles
) {
}
