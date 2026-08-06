package com.yasarbilgi.ats.auth.entity;

import com.yasarbilgi.ats.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder @Entity
@Table(name = "platform_admins", uniqueConstraints = @UniqueConstraint(
        name = "uk_platform_admins_email", columnNames = "email"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformAdmin extends BaseEntity {
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
