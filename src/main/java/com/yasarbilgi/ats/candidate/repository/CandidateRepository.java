package com.yasarbilgi.ats.candidate.repository;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.time.Instant;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // Şirketteki aktif adayların toplam sayısını getirir.
    long countByCompanyIdAndActiveTrue(Long companyId);

    long countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(Long companyId, Instant periodStart);

    @Query("""
            SELECT COUNT(DISTINCT candidate.id) FROM Candidate candidate
            JOIN CandidateProcess process ON process.candidate = candidate
            WHERE candidate.company.id = :companyId AND candidate.active = true
              AND candidate.createdAt >= :periodStart AND process.active = true
              AND process.position.department.id IN :departmentIds
            """)
    long countNewCandidatesByDepartmentIds(@Param("companyId") Long companyId,
                                            @Param("departmentIds") Set<Long> departmentIds,
                                            @Param("periodStart") Instant periodStart);

    // İzin verilen departmanlarda aktif süreci bulunan benzersiz adayların sayısını getirir.
    @Query("""
            SELECT COUNT(DISTINCT candidate.id) FROM Candidate candidate
            JOIN CandidateProcess process ON process.candidate = candidate
            WHERE candidate.company.id = :companyId AND candidate.active = true AND process.active = true
              AND process.position.department.id IN :departmentIds
            """)
    long countActiveCandidatesByDepartmentIds(@Param("companyId") Long companyId,
                                              @Param("departmentIds") Set<Long> departmentIds);

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
                    CAST(:search AS string) IS NULL
                    OR LOWER(CONCAT(candidate.firstName, ' ', candidate.lastName))
                        LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(candidate.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(candidate.linkedinUrl) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              )
            """)
    Page<Candidate> searchActiveCandidates(
            @Param("companyId") Long companyId,
            @Param("search") String search,
            Pageable pageable
    );

    // Adayları kullanıcının yönetebildiği departmanlardaki aktif süreçlerle sınırlandırarak arar.
    @Query("""
            SELECT DISTINCT candidate
            FROM Candidate candidate
            JOIN CandidateProcess process ON process.candidate = candidate
            WHERE candidate.company.id = :companyId
              AND candidate.active = true
              AND process.active = true
              AND process.position.department.id IN :departmentIds
              AND (
                    CAST(:search AS string) IS NULL
                    OR LOWER(CONCAT(candidate.firstName, ' ', candidate.lastName))
                        LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(candidate.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                    OR LOWER(candidate.linkedinUrl) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              )
            """)
    Page<Candidate> searchActiveCandidatesByDepartmentIds(
            @Param("companyId") Long companyId,
            @Param("departmentIds") Set<Long> departmentIds,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
            SELECT candidate FROM Candidate candidate
            WHERE candidate.company.id = :companyId
              AND (:includeInactive = true OR candidate.active = true)
              AND (CAST(:search AS string) IS NULL
                   OR LOWER(CONCAT(candidate.firstName, ' ', candidate.lastName)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(candidate.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(candidate.linkedinUrl) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<Candidate> searchCandidates(@Param("companyId") Long companyId,
                                     @Param("search") String search,
                                     @Param("includeInactive") boolean includeInactive,
                                     Pageable pageable);
}
