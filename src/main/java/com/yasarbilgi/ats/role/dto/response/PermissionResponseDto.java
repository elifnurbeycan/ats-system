package com.yasarbilgi.ats.role.dto.response;

import com.yasarbilgi.ats.permission.entity.PermissionCategory;
import com.yasarbilgi.ats.permission.entity.PermissionCode;

public record PermissionResponseDto(
        Long id,
        PermissionCode code,
        String name,
        String description,
        PermissionCategory category,
        Integer displayOrder
) {}
