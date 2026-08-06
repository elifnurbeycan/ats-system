package com.yasarbilgi.ats.department.repository;

import com.yasarbilgi.ats.department.entity.DepartmentManagerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentManagerAssignmentRepository
        extends JpaRepository<DepartmentManagerAssignment, Long> {

    Optional<DepartmentManagerAssignment>
    findByCompanyIdAndDepartmentIdAndUserIdAndActiveTrue(
            Long companyId,
            Long departmentId,
            Long userId
    );

    boolean existsByCompanyIdAndDepartmentIdAndUserIdAndActiveTrue(
            Long companyId,
            Long departmentId,
            Long userId
    );

    List<DepartmentManagerAssignment> findAllByCompanyIdAndDepartmentIdAndActiveTrue(
            Long companyId,
            Long departmentId
    );

    List<DepartmentManagerAssignment> findAllByCompanyIdAndUserIdAndActiveTrue(
            Long companyId,
            Long userId
    );
}
