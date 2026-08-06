package com.yasarbilgi.ats.user.repository;

import com.yasarbilgi.ats.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    // Şirkete ait kullanıcıları departman ve rolleriyle birlikte getirir.
    @EntityGraph(attributePaths = {"department", "roles"})
    List<User> findAllByCompanyIdOrderByFirstNameAscLastNameAsc(Long companyId);

    // Şirkete ve departmana ait kullanıcıları departman ve rolleriyle birlikte getirir.
    @EntityGraph(attributePaths = {"department", "roles"})
    List<User> findAllByCompanyIdAndDepartmentIdOrderByFirstNameAscLastNameAsc(
            Long companyId,
            Long departmentId
    );

    // Şirkete ait kullanıcıyı departman ve rolleriyle birlikte getirir.
    @EntityGraph(attributePaths = {"department", "roles"})
    Optional<User> findWithDetailsByCompanyIdAndId(Long companyId, Long userId);
}
