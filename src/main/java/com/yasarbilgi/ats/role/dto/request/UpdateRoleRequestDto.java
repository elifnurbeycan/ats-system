package com.yasarbilgi.ats.role.dto.request;

import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.role.entity.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateRoleRequestDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull DataScope dataScope,
        @NotEmpty Set<PermissionCode> permissions
) {}
