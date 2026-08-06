package com.yasarbilgi.ats.followup.entity;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder
@Entity
@Table(name = "candidate_follow_ups", indexes = {
        @Index(name = "idx_follow_ups_candidate", columnList = "candidate_id"),
        @Index(name = "idx_follow_ups_process", columnList = "candidate_process_id"),
        @Index(name = "idx_follow_ups_assigned_to", columnList = "assigned_to_id"),
        @Index(name = "idx_follow_ups_due_at", columnList = "due_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowUp extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_process_id")
    private CandidateProcess candidateProcess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FollowUpStatus status;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Bekleyen takip görevinin planlama ve sorumlu bilgilerini günceller.
    public void updateDetails(String title, String description, Instant dueAt, User assignedTo) {
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.assignedTo = assignedTo;
    }

    // Takip görevini tamamlandı olarak işaretler.
    public void complete() {
        this.status = FollowUpStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    // Takip görevini iptal eder.
    public void cancel() {
        this.status = FollowUpStatus.CANCELLED;
        this.completedAt = null;
    }
}
