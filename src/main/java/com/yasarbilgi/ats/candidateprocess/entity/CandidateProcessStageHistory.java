package com.yasarbilgi.ats.candidateprocess.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
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

@Getter
@SuperBuilder
@Entity
@Table(
        name = "candidate_process_stage_history",
        indexes = {
                @Index(
                        name = "idx_stage_history_candidate_process",
                        columnList = "candidate_process_id"
                ),
                @Index(
                        name = "idx_stage_history_to_stage",
                        columnList = "to_stage_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateProcessStageHistory extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_process_id",
            nullable = false
    )
    private CandidateProcess candidateProcess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stage_id")
    private PipelineStage fromStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "to_stage_id",
            nullable = false
    )
    private PipelineStage toStage;

    @Column(
            name = "reason",
            length = 500
    )
    private String reason;
}