package com.yasarbilgi.ats.permission.repository;

import com.yasarbilgi.ats.permission.entity.Permission;
import com.yasarbilgi.ats.permission.entity.PermissionCategory;
import com.yasarbilgi.ats.permission.entity.PermissionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(PermissionCode code);

    boolean existsByCode(PermissionCode code);

    List<Permission> findAllByCategoryAndActiveTrueOrderByDisplayOrderAsc(
            PermissionCategory category
    );

    // Sistem rollerini kurmak için tüm aktif permission kayıtlarını getirir.
    List<Permission> findAllByActiveTrueOrderByDisplayOrderAsc();
}
