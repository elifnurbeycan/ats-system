package com.yasarbilgi.ats.permission.entity;

import com.yasarbilgi.ats.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permissions_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_permissions_category",
                        columnList = "category"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(
            name = "code",
            nullable = false,
            updatable = false,
            length = 100
    )
    private PermissionCode code;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            nullable = false,
            length = 50
    )
    private PermissionCategory category;

    @Builder.Default
    @Column(
            name = "system_permission",
            nullable = false
    )
    private boolean systemPermission = true;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;
}
