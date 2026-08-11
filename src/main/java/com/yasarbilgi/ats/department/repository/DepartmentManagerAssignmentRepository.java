package com.yasarbilgi.ats.department.repository;

import com.yasarbilgi.ats.department.entity.DepartmentManagerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Atamayı departman ve kullanıcı ayrıntılarıyla şirket sınırında getirir.
    @EntityGraph(attributePaths = {"department", "user"})
    Optional<DepartmentManagerAssignment> findWithDetailsByCompanyIdAndDepartmentIdAndId(
            Long companyId,
            Long departmentId,
            Long assignmentId
    );

    // Departmanın aktif yönetici atamalarını kullanıcı ayrıntılarıyla getirir.
    @EntityGraph(attributePaths = {"department", "user"})
    List<DepartmentManagerAssignment>
    findAllByCompanyIdAndDepartmentIdAndActiveTrueOrderByStartedAtDesc(
            Long companyId,
            Long departmentId
    );

    @EntityGraph(attributePaths = {"user"})
    List<DepartmentManagerAssignment>
    findAllByCompanyIdAndDepartmentIdAndActiveTrueAndEndedAtIsNullOrderByStartedAtDesc(
            Long companyId,
            Long departmentId
    );

    // Departmanın geçmiş dahil tüm yönetici atamalarını kullanıcı ayrıntılarıyla getirir.
    @EntityGraph(attributePaths = {"department", "user"})
    List<DepartmentManagerAssignment>
    findAllByCompanyIdAndDepartmentIdOrderByStartedAtDesc(
            Long companyId,
            Long departmentId
    );

    @EntityGraph(attributePaths = {"department", "user"})
    Page<DepartmentManagerAssignment> findAllByCompanyIdAndDepartmentId(
            Long companyId, Long departmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"department", "user"})
    Page<DepartmentManagerAssignment> findAllByCompanyIdAndDepartmentIdAndActiveTrue(
            Long companyId, Long departmentId, Pageable pageable);
}
