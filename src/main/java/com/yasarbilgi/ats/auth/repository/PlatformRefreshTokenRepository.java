package com.yasarbilgi.ats.auth.repository;
import com.yasarbilgi.ats.auth.entity.PlatformRefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PlatformRefreshTokenRepository extends JpaRepository<PlatformRefreshToken, Long> {
    // Platform refresh tokenını yönetici bilgisiyle getirir.
    @EntityGraph(attributePaths = "platformAdmin")
    Optional<PlatformRefreshToken> findByTokenHash(String tokenHash);
}
