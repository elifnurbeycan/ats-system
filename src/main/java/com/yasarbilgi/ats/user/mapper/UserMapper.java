package com.yasarbilgi.ats.user.mapper;

import com.yasarbilgi.ats.role.mapper.RoleMapper;
import com.yasarbilgi.ats.user.dto.response.UserResponseDto;
import com.yasarbilgi.ats.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {

    // Kullanıcı entity'sini departman ve rol bilgileriyle API yanıtına dönüştürür.
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    UserResponseDto toResponseDto(User user);
}
