package com.yasarbilgi.ats.role.mapper;

import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.entity.Role;
import org.mapstruct.Mapper;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    // Rol entity'sini API yanıt modeline dönüştürür.
    default RoleResponseDto toResponseDto(Role role) {
        var permissions = role.getPermissions().stream()
                .sorted(Comparator.comparing(permission -> permission.getDisplayOrder()))
                .map(permission -> new com.yasarbilgi.ats.role.dto.response.PermissionResponseDto(
                        permission.getId(), permission.getCode(), permission.getName(),
                        permission.getDescription(), permission.getCategory(), permission.getDisplayOrder()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RoleResponseDto(role.getId(), role.getCode(), role.getName(), role.getDescription(),
                role.getDataScope(), role.isSystemRole(), permissions);
    }
}
