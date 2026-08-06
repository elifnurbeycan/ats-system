package com.yasarbilgi.ats.department.mapper;

import com.yasarbilgi.ats.department.dto.response.DepartmentResponseDto;
import com.yasarbilgi.ats.department.entity.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    // Departman entity'sini API yanıt modeline dönüştürür.
    DepartmentResponseDto toResponseDto(Department department);
}
