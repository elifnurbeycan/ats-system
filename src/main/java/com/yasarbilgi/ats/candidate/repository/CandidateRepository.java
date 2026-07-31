package com.yasarbilgi.ats.candidate.repository;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByCompanyIdAndId(
            Long companyId,
            Long candidateId
    );

    Optional<Candidate> findByCompanyIdAndLinkedinUrl(
            Long companyId,
            String linkedinUrl
    );

    boolean existsByCompanyIdAndLinkedinUrl(
            Long companyId,
            String linkedinUrl
    );

    Page<Candidate> findAllByCompanyIdAndActiveTrue(
            Long companyId,
            Pageable pageable
    );
}