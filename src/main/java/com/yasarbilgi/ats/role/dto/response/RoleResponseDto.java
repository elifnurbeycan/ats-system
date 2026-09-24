package com.yasarbilgi.ats.role.dto.response;

import com.yasarbilgi.ats.role.entity.DataScope;

import java.util.Set;

public record RoleResponseDto(
        Long id,
        String code,
        String name,
        String description,
        DataScope dataScope,
        boolean systemRole,
        Set<PermissionResponseDto> permissions
) {
}
