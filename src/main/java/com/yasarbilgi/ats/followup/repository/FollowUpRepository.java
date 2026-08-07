package com.yasarbilgi.ats.followup.repository;

import com.yasarbilgi.ats.followup.entity.FollowUp;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    @Query("""
            SELECT followUp FROM FollowUp followUp
            WHERE followUp.company.id = :companyId AND followUp.candidate.id = :candidateId
              AND followUp.active = true
              AND (:status IS NULL OR followUp.status = :status)
              AND (:assignedToUserId IS NULL OR followUp.assignedTo.id = :assignedToUserId)
            """)
    Page<FollowUp> searchActive(@Param("companyId") Long companyId,
                                @Param("candidateId") Long candidateId,
                                @Param("status") FollowUpStatus status,
                                @Param("assignedToUserId") Long assignedToUserId,
                                Pageable pageable);

    // Şirkette bekleyen aktif takip görevlerinin sayısını getirir.
    long countByCompanyIdAndStatusAndActiveTrue(Long companyId, FollowUpStatus status);

    // İzin verilen departmanlardaki aday süreçlerine bağlı bekleyen takip görevlerini sayar.
    long countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndActiveTrue(
            Long companyId, Set<Long> departmentIds, FollowUpStatus status);

    // Son tarihi geçmiş bekleyen aktif takip görevlerinin sayısını getirir.
    long countByCompanyIdAndStatusAndDueAtBeforeAndActiveTrue(
            Long companyId, FollowUpStatus status, Instant dueAt);

    // İzin verilen departmanlardaki gecikmiş bekleyen takip görevlerini sayar.
    long countByCompanyIdAndCandidateProcessPositionDepartmentIdInAndStatusAndDueAtBeforeAndActiveTrue(
            Long companyId, Set<Long> departmentIds, FollowUpStatus status, Instant dueAt);

    // Takip görevini ilişkileriyle şirket ve aday sınırında getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    Optional<FollowUp> findWithDetailsByCompanyIdAndCandidateIdAndId(
            Long companyId, Long candidateId, Long followUpId);

    // Adayın aktif takip görevlerini son tarihe göre getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    List<FollowUp> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByDueAtAsc(
            Long companyId, Long candidateId);
}
