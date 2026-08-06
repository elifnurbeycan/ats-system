package com.yasarbilgi.ats.role.dto.response;

import com.yasarbilgi.ats.role.entity.DataScope;

public record RoleResponseDto(
        Long id,
        String code,
        String name,
        String description,
        DataScope dataScope
) {
}
