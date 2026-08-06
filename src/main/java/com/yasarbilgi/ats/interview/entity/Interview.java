package com.yasarbilgi.ats.interview.entity;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Table(name = "interviews", indexes = {
        @Index(name = "idx_interviews_process", columnList = "candidate_process_id"),
        @Index(name = "idx_interviews_scheduled_at", columnList = "scheduled_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_process_id", nullable = false)
    private CandidateProcess candidateProcess;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private InterviewMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterviewStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "location", length = 300)
    private String location;

    @Column(name = "meeting_url", length = 1000)
    private String meetingUrl;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "interview_interviewers",
            joinColumns = @JoinColumn(name = "interview_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_interview_interviewers", columnNames = {"interview_id", "user_id"}))
    private Set<User> interviewers = new HashSet<>();

    // Görüşmenin planlama bilgilerini ve atanmış görüşmecilerini günceller.
    public void updateSchedule(InterviewType type, InterviewMode mode, Instant scheduledAt,
                               Integer durationMinutes, String location, String meetingUrl,
                               Set<User> interviewers) {
        this.type = type;
        this.mode = mode;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.location = location;
        this.meetingUrl = meetingUrl;
        this.interviewers.clear();
        this.interviewers.addAll(interviewers);
    }

    // Görüşmenin planlandı, tamamlandı veya iptal edildi durumunu değiştirir.
    public void changeStatus(InterviewStatus status) { this.status = status; }
}
