package com.yasarbilgi.ats.candidateprocess.repository;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcessStageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateProcessStageHistoryRepository
        extends JpaRepository<CandidateProcessStageHistory, Long> {

    @Query("""
            SELECT COUNT(DISTINCT history.candidateProcess.id)
            FROM CandidateProcessStageHistory history
            WHERE history.company.id = :companyId AND history.createdAt >= :periodStart
              AND history.toStage.stageType = :stageType
            """)
    long countTransitionsByStageType(@Param("companyId") Long companyId,
                                     @Param("periodStart") Instant periodStart,
                                     @Param("stageType") PipelineStageType stageType);

    @Query("""
            SELECT COUNT(DISTINCT history.candidateProcess.id)
            FROM CandidateProcessStageHistory history
            WHERE history.company.id = :companyId AND history.createdAt >= :periodStart
              AND history.toStage.stageType = :stageType
              AND history.candidateProcess.position.department.id IN :departmentIds
            """)
    long countTransitionsByStageTypeAndDepartmentIds(@Param("companyId") Long companyId,
                                                      @Param("departmentIds") Set<Long> departmentIds,
                                                      @Param("periodStart") Instant periodStart,
                                                      @Param("stageType") PipelineStageType stageType);

    Page<CandidateProcessStageHistory>
    findAllByCompanyIdAndCandidateProcessIdOrderByCreatedAtDesc(
            Long companyId,
            Long candidateProcessId,
            Pageable pageable
    );

    Optional<CandidateProcessStageHistory>
    findFirstByCompanyIdAndCandidateProcessIdOrderByCreatedAtDesc(
            Long companyId,
            Long candidateProcessId
    );

    // Sürecin aşama geçmişini eski kayıttan yeni kayda doğru aşama detaylarıyla getirir.
    @EntityGraph(attributePaths = {"fromStage", "toStage"})
    List<CandidateProcessStageHistory>
    findAllByCompanyIdAndCandidateProcessIdOrderByCreatedAtAsc(
            Long companyId,
            Long candidateProcessId
    );
}
