package com.yasarbilgi.ats.role.repository;

import com.yasarbilgi.ats.role.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCompanyIdAndId(Long companyId, Long roleId);

    Optional<Role> findByCompanyIdAndCodeIgnoreCase(Long companyId, String code);

    boolean existsByCompanyIdAndCodeIgnoreCase(Long companyId, String code);

    Page<Role> findAllByCompanyIdAndActiveTrue(
            Long companyId,
            Pageable pageable
    );

    // Şirkete ait aktif rolleri görünen adlarına göre sıralayarak getirir.
    List<Role> findAllByCompanyIdAndActiveTrueOrderByNameAsc(Long companyId);

    // Verilen rol kimliklerinden şirkete ait ve aktif olanları getirir.
    List<Role> findAllByCompanyIdAndIdInAndActiveTrue(Long companyId, Set<Long> roleIds);
}
