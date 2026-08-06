package com.yasarbilgi.ats.pipeline.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
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

@Getter
@SuperBuilder
@Entity
@Table(
        name = "pipeline_stages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pipeline_stages_pipeline_code",
                        columnNames = {"pipeline_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uk_pipeline_stages_pipeline_order",
                        columnNames = {"pipeline_id", "display_order"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PipelineStage extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pipeline_id",
            nullable = false
    )
    private RecruitmentPipeline pipeline;

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

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "stage_type",
            nullable = false,
            length = 30
    )
    private PipelineStageType stageType;

    // Pipeline aşamasının kullanıcıya gösterilen bilgilerini günceller.
    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Pipeline aşamasının sıralamadaki yerini değiştirir.
    public void changeDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // Pipeline aşamasının sistem içerisindeki davranış türünü değiştirir.
    public void changeStageType(PipelineStageType stageType) {
        this.stageType = stageType;
    }
}
