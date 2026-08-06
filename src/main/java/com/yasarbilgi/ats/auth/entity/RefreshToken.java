package com.yasarbilgi.ats.auth.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@Entity
@Table(name = "auth_refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
}, uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_hash", columnNames = "token_hash"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends TenantBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;

    // Refresh token kaydını tekrar kullanılamayacak şekilde iptal eder.
    public void revoke() { if (revokedAt == null) revokedAt = Instant.now(); }
    // Refresh tokenın geçerli, süresi dolmamış ve iptal edilmemiş olduğunu kontrol eder.
    public boolean isUsable(Instant now) { return isActive() && revokedAt == null && expiresAt.isAfter(now); }
}
