package com.yasarbilgi.ats.role.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.permission.entity.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_roles_company_code",
                        columnNames = {"company_id", "code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_roles_company",
                        columnList = "company_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends TenantBaseEntity {

    @Column(
            name = "code",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "data_scope",
            nullable = false,
            length = 30
    )
    private DataScope dataScope;

    @Builder.Default
    @Column(
            name = "system_role",
            nullable = false
    )
    private boolean systemRole = true;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permissions_role_permission",
                    columnNames = {"role_id", "permission_id"}
            )
    )
    private Set<Permission> permissions = new HashSet<>();

    // Role yeni bir yetki ekler.
    public void assignPermission(Permission permission) {
        permissions.add(permission);
    }

    // Rolden mevcut bir yetkiyi kaldırır.
    public void revokePermission(Permission permission) {
        permissions.remove(permission);
    }

    // Rolün belirtilen yetkiye sahip olup olmadığını kontrol eder.
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
}
