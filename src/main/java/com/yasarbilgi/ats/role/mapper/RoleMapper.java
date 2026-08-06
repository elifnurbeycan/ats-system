package com.yasarbilgi.ats.role.mapper;

import com.yasarbilgi.ats.role.dto.response.RoleResponseDto;
import com.yasarbilgi.ats.role.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    // Rol entity'sini API yanıt modeline dönüştürür.
    RoleResponseDto toResponseDto(Role role);
}
