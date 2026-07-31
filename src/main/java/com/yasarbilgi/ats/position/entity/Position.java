package com.yasarbilgi.ats.position.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.department.entity.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "positions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_positions_company_code",
                        columnNames = {"company_id", "code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;

    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;

    @Column(
            name = "code",
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "vacancy_count",
            nullable = false
    )
    private Integer vacancyCount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private PositionStatus status;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    // Pozisyonun temel bilgilerini günceller.
    public void updateDetails(
            String title,
            String description,
            Integer vacancyCount,
            Department department
    ) {
        this.title = title;
        this.description = description;
        this.vacancyCount = vacancyCount;
        this.department = department;
    }

    // Pozisyonu aday kabul edecek şekilde açar.
    public void open() {
        this.status = PositionStatus.OPEN;
        this.openedAt = Instant.now();
        this.closedAt = null;
    }

    // Pozisyonun işe alım sürecini geçici olarak beklemeye alır.
    public void putOnHold() {
        this.status = PositionStatus.ON_HOLD;
    }

    // Pozisyonun işe alım sürecini tamamlanmış olarak kapatır.
    public void close() {
        this.status = PositionStatus.CLOSED;
        this.closedAt = Instant.now();
    }

    // Pozisyonu iptal edilmiş duruma getirir.
    public void cancel() {
        this.status = PositionStatus.CANCELLED;
        this.closedAt = Instant.now();
    }
}