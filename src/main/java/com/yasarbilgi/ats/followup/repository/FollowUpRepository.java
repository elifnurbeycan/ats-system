package com.yasarbilgi.ats.followup.repository;

import com.yasarbilgi.ats.followup.entity.FollowUp;
import com.yasarbilgi.ats.followup.entity.FollowUpStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    // Şirkette bekleyen aktif takip görevlerinin sayısını getirir.
    long countByCompanyIdAndStatusAndActiveTrue(Long companyId, FollowUpStatus status);

    // Son tarihi geçmiş bekleyen aktif takip görevlerinin sayısını getirir.
    long countByCompanyIdAndStatusAndDueAtBeforeAndActiveTrue(
            Long companyId, FollowUpStatus status, Instant dueAt);

    // Takip görevini ilişkileriyle şirket ve aday sınırında getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    Optional<FollowUp> findWithDetailsByCompanyIdAndCandidateIdAndId(
            Long companyId, Long candidateId, Long followUpId);

    // Adayın aktif takip görevlerini son tarihe göre getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    List<FollowUp> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByDueAtAsc(
            Long companyId, Long candidateId);
}
