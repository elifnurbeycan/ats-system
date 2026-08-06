package com.yasarbilgi.ats.department.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_departments_company_code",
                        columnNames = {"company_id", "code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department extends TenantBaseEntity {

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            name = "code",
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    // Departmanın görünen adı ve açıklamasını birlikte günceller.
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
