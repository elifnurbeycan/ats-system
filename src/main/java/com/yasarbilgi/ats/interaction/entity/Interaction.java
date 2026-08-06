package com.yasarbilgi.ats.interaction.entity;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "candidate_interactions",
        indexes = {
                @Index(name = "idx_interactions_company", columnList = "company_id"),
                @Index(name = "idx_interactions_candidate", columnList = "candidate_id"),
                @Index(name = "idx_interactions_process", columnList = "candidate_process_id"),
                @Index(name = "idx_interactions_occurred_at", columnList = "occurred_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interaction extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_process_id")
    private CandidateProcess candidateProcess;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private InteractionChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 20)
    private InteractionDirection direction;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    // İletişim kaydının düzenlenebilir ayrıntılarını günceller.
    public void updateDetails(
            InteractionChannel channel,
            InteractionDirection direction,
            Instant occurredAt,
            String subject,
            String summary
    ) {
        this.channel = channel;
        this.direction = direction;
        this.occurredAt = occurredAt;
        this.subject = subject;
        this.summary = summary;
    }
}
