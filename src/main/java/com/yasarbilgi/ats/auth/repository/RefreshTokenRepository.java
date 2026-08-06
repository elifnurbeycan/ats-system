package com.yasarbilgi.ats.auth.repository;
import com.yasarbilgi.ats.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // Token özetine ait kaydı kullanıcı ve rolleriyle getirir.
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
