package com.yasarbilgi.ats.department.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "department_manager_assignments",
        indexes = {
                @Index(
                        name = "idx_department_manager_assignments_department",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_department_manager_assignments_user",
                        columnList = "user_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DepartmentManagerAssignment extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "department_id",
            nullable = false
    )
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "started_at",
            nullable = false
    )
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    // Yöneticinin departman üzerindeki aktif görevlendirmesini sona erdirir.
    public void endAssignment() {
        if (endedAt != null) {
            return;
        }

        endedAt = Instant.now();
        deactivate();
    }
}
