package com.yasarbilgi.ats.department.mapper;

import com.yasarbilgi.ats.department.dto.response.DepartmentManagerAssignmentResponseDto;
import com.yasarbilgi.ats.department.entity.DepartmentManagerAssignment;
import org.springframework.stereotype.Component;

@Component
public class DepartmentManagerAssignmentMapper {

    // Yönetici atamasını departman ve kullanıcı bilgileriyle API yanıtına dönüştürür.
    public DepartmentManagerAssignmentResponseDto toResponseDto(
            DepartmentManagerAssignment assignment
    ) {
        return new DepartmentManagerAssignmentResponseDto(
                assignment.getId(),
                assignment.getDepartment().getId(),
                assignment.getDepartment().getName(),
                assignment.getUser().getId(),
                assignment.getUser().getFullName(),
                assignment.getUser().getEmail(),
                assignment.getStartedAt(),
                assignment.getEndedAt(),
                assignment.isActive()
        );
    }
}
