package com.yasarbilgi.ats.pipeline.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "recruitment_pipelines",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recruitment_pipelines_company_code",
                        columnNames = {"company_id", "code"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentPipeline extends TenantBaseEntity {

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

    @Builder.Default
    @Column(
            name = "default_pipeline",
            nullable = false
    )
    private boolean defaultPipeline = false;

    // Pipeline'ın görünen adını ve açıklamasını günceller.
    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Pipeline'ı şirketin varsayılan işe alım süreci olarak işaretler.
    public void markAsDefault() {
        this.defaultPipeline = true;
    }

    // Pipeline'ın varsayılan işe alım süreci işaretini kaldırır.
    public void removeDefault() {
        this.defaultPipeline = false;
    }
}