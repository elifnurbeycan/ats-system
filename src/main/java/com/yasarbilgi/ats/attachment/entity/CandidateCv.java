package com.yasarbilgi.ats.attachment.entity;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.common.base.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "candidate_cvs", uniqueConstraints =
        @UniqueConstraint(name = "uk_candidate_cvs_candidate", columnNames = "candidate_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateCv extends TenantBaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_key", nullable = false, unique = true, length = 100)
    private String storageKey;

    public void replace(String originalFileName, String contentType, long fileSize, String storageKey) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storageKey = storageKey;
    }
}
