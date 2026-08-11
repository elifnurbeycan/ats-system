package com.yasarbilgi.ats.attachment.repository;

import com.yasarbilgi.ats.attachment.entity.CandidateCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, Long> {
    Optional<CandidateCv> findByCompanyIdAndCandidateId(Long companyId, Long candidateId);
}
