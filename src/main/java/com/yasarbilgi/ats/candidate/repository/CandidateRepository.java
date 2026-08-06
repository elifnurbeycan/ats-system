package com.yasarbilgi.ats.candidate.repository;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Aktif adayları ad, e-posta veya LinkedIn bilgisine göre sayfalı olarak arar.
    @Query("""
            SELECT candidate
            FROM Candidate candidate
            WHERE candidate.company.id = :companyId
              AND candidate.active = true
              AND (
                    :search IS NULL
                    OR LOWER(CONCAT(candidate.firstName, ' ', candidate.lastName))
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(candidate.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(candidate.linkedinUrl) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Candidate> searchActiveCandidates(
            @Param("companyId") Long companyId,
            @Param("search") String search,
            Pageable pageable
    );
}
