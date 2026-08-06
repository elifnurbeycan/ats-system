package com.yasarbilgi.ats.candidateprocess.repository;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

public interface CandidateProcessRepository
        extends JpaRepository<CandidateProcess, Long> {

    Optional<CandidateProcess> findByCompanyIdAndId(
            Long companyId,
            Long candidateProcessId
    );

    Page<CandidateProcess> findAllByCompanyIdAndCandidateIdAndActiveTrue(
            Long companyId,
            Long candidateId,
            Pageable pageable
    );

    Page<CandidateProcess> findAllByCompanyIdAndPositionIdAndActiveTrue(
            Long companyId,
            Long positionId,
            Pageable pageable
    );

    Page<CandidateProcess> findAllByCompanyIdAndPositionIdAndCurrentStageIdAndActiveTrue(
            Long companyId,
            Long positionId,
            Long currentStageId,
            Pageable pageable
    );

    List<CandidateProcess> findAllByCompanyIdAndPositionIdAndPipelineIdAndActiveTrue(
            Long companyId,
            Long positionId,
            Long pipelineId
    );

    // Adayın aktif süreçlerini pozisyon, pipeline ve aşama bilgileriyle getirir.
    @EntityGraph(attributePaths = {"position", "pipeline", "currentStage"})
    List<CandidateProcess> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
            Long companyId,
            Long candidateId
    );
}
