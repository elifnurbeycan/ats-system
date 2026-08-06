package com.yasarbilgi.ats.followup.repository;

import com.yasarbilgi.ats.followup.entity.FollowUp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    // Takip görevini ilişkileriyle şirket ve aday sınırında getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    Optional<FollowUp> findWithDetailsByCompanyIdAndCandidateIdAndId(
            Long companyId, Long candidateId, Long followUpId);

    // Adayın aktif takip görevlerini son tarihe göre getirir.
    @EntityGraph(attributePaths = {"candidate", "candidateProcess", "assignedTo"})
    List<FollowUp> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByDueAtAsc(
            Long companyId, Long candidateId);
}
