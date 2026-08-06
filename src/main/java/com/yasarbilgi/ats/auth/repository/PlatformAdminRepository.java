package com.yasarbilgi.ats.auth.repository;
import com.yasarbilgi.ats.auth.entity.PlatformAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin, Long> {
    // Platform yöneticisini e-posta adresine göre getirir.
    Optional<PlatformAdmin> findByEmailIgnoreCase(String email);
}
