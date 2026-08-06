package com.yasarbilgi.ats.interview.entity;

import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import com.yasarbilgi.ats.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "interview_evaluations", uniqueConstraints = @UniqueConstraint(
        name = "uk_interview_evaluator", columnNames = {"interview_id", "evaluator_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewEvaluation extends TenantBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;
    @Column(name = "score", nullable = false)
    private Integer score;
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 30)
    private InterviewRecommendation recommendation;
    @Column(name = "feedback", nullable = false, columnDefinition = "TEXT")
    private String feedback;

    // Görüşmecinin puan, öneri ve geri bildirimini günceller.
    public void update(Integer score, InterviewRecommendation recommendation, String feedback) {
        this.score = score;
        this.recommendation = recommendation;
        this.feedback = feedback;
    }
}
