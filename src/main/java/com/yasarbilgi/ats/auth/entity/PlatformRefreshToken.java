package com.yasarbilgi.ats.auth.entity;

import com.yasarbilgi.ats.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Getter @SuperBuilder @Entity
@Table(name = "platform_refresh_tokens", uniqueConstraints = @UniqueConstraint(
        name = "uk_platform_refresh_tokens_hash", columnNames = "token_hash"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformRefreshToken extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_admin_id", nullable = false)
    private PlatformAdmin platformAdmin;
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;
    // Platform refresh tokenını tekrar kullanılamayacak şekilde iptal eder.
    public void revoke() { if (revokedAt == null) revokedAt = Instant.now(); }
    // Tokenın aktif, iptal edilmemiş ve süresi dolmamış olduğunu kontrol eder.
    public boolean isUsable(Instant now) { return isActive() && revokedAt == null && expiresAt.isAfter(now); }
}
