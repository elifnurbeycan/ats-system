package com.yasarbilgi.ats.candidatenote.entity;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
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
        name = "candidate_notes",
        indexes = {
                @Index(name = "idx_candidate_notes_company", columnList = "company_id"),
                @Index(name = "idx_candidate_notes_candidate", columnList = "candidate_id"),
                @Index(name = "idx_candidate_notes_process", columnList = "candidate_process_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateNote extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_process_id")
    private CandidateProcess candidateProcess;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // Notun metin içeriğini günceller.
    public void updateContent(String content) {
        this.content = content;
    }
}
