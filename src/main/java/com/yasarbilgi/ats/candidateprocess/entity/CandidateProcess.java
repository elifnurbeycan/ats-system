package com.yasarbilgi.ats.candidateprocess.entity;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import com.yasarbilgi.ats.position.entity.Position;
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

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@SuperBuilder
@Entity
@Table(
        name = "candidate_processes",
        indexes = {
                @Index(
                        name = "idx_candidate_processes_company",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_candidate_processes_position",
                        columnList = "position_id"
                ),
                @Index(
                        name = "idx_candidate_processes_current_stage",
                        columnList = "current_stage_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateProcess extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false
    )
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "position_id",
            nullable = false
    )
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pipeline_id",
            nullable = false
    )
    private RecruitmentPipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "current_stage_id",
            nullable = false
    )
    private PipelineStage currentStage;

    @Column(
            name = "current_salary",
            precision = 19,
            scale = 2
    )
    private BigDecimal currentSalary;

    @Column(
            name = "expected_salary",
            precision = 19,
            scale = 2
    )
    private BigDecimal expectedSalary;

    @Column(
            name = "offered_salary",
            precision = 19,
            scale = 2
    )
    private BigDecimal offeredSalary;

    @Column(
            name = "salary_currency",
            length = 3
    )
    private String salaryCurrency;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Adayın süreç başlangıcındaki mevcut maaş bilgisini günceller.
    public void updateCurrentSalary(
            BigDecimal currentSalary,
            String salaryCurrency
    ) {
        this.currentSalary = currentSalary;
        this.salaryCurrency = salaryCurrency;
    }

    // Adayı yeni aşamaya taşır ve sürecin tamamlanma zamanını düzenler.
    public void changeStage(PipelineStage newStage) {
        this.currentStage = newStage;

        PipelineStageType stageType = newStage.getStageType();

        if (stageType == PipelineStageType.HIRED
                || stageType == PipelineStageType.REJECTED) {
            this.completedAt = Instant.now();
            return;
        }

        this.completedAt = null;
    }

    // Adayın maaş beklentisini günceller.
    public void updateExpectedSalary(
            BigDecimal expectedSalary,
            String salaryCurrency
    ) {
        this.expectedSalary = expectedSalary;
        this.salaryCurrency = salaryCurrency;
    }

    // Adaya sunulan maaş teklifini günceller.
    public void updateOfferedSalary(
            BigDecimal offeredSalary,
            String salaryCurrency
    ) {
        this.offeredSalary = offeredSalary;
        this.salaryCurrency = salaryCurrency;
    }
}
