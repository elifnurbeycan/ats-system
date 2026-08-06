package com.yasarbilgi.ats.user.repository;

import com.yasarbilgi.ats.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCompanyIdAndId(Long companyId, Long userId);

    Optional<User> findByCompanyIdAndEmailIgnoreCase(Long companyId, String email);

    boolean existsByCompanyIdAndEmailIgnoreCase(Long companyId, String email);

    Page<User> findAllByCompanyIdAndActiveTrue(
            Long companyId,
            Pageable pageable
    );

    Page<User> findAllByCompanyIdAndDepartmentIdAndActiveTrue(
            Long companyId,
            Long departmentId,
            Pageable pageable
    );
}
