package com.yasarbilgi.ats.candidateprocess.repository;

import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.time.Instant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CandidateProcessRepository
        extends JpaRepository<CandidateProcess, Long> {

    // Şirkette henüz tamamlanmamış aktif aday süreçlerinin sayısını getirir.
    long countByCompanyIdAndActiveTrueAndCompletedAtIsNull(Long companyId);

    long countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(Long companyId, Instant periodStart);

    long countByCompanyIdAndPositionDepartmentIdInAndActiveTrueAndCreatedAtGreaterThanEqual(
            Long companyId, Set<Long> departmentIds, Instant periodStart);

    // İzin verilen departmanlardaki tamamlanmamış aktif süreçleri sayar.
    long countByCompanyIdAndPositionDepartmentIdInAndActiveTrueAndCompletedAtIsNull(
            Long companyId, Set<Long> departmentIds);

    // Aktif aday süreçlerini mevcut pipeline aşamalarına göre gruplandırır.
    @Query("""
            SELECT stage.id AS stageId, stage.name AS stageName, stage.code AS stageCode,
                   stage.displayOrder AS displayOrder, stage.stageType AS stageType,
                   COUNT(process.id) AS candidateCount
            FROM CandidateProcess process
            JOIN process.currentStage stage
            WHERE process.company.id = :companyId
              AND process.active = true
            GROUP BY stage.id, stage.name, stage.code, stage.displayOrder, stage.stageType
            ORDER BY stage.displayOrder
            """)
    List<StageCandidateCountProjection> countActiveProcessesByStage(@Param("companyId") Long companyId);

    // İzin verilen departmanlardaki aktif süreçleri mevcut aşamalarına göre gruplandırır.
    @Query("""
            SELECT stage.id AS stageId, stage.name AS stageName, stage.code AS stageCode,
                   stage.displayOrder AS displayOrder, stage.stageType AS stageType,
                   COUNT(process.id) AS candidateCount
            FROM CandidateProcess process JOIN process.currentStage stage
            WHERE process.company.id = :companyId AND process.active = true
              AND process.position.department.id IN :departmentIds
            GROUP BY stage.id, stage.name, stage.code, stage.displayOrder, stage.stageType ORDER BY stage.displayOrder
            """)
    List<StageCandidateCountProjection> countActiveProcessesByStageAndDepartmentIds(
            @Param("companyId") Long companyId, @Param("departmentIds") Set<Long> departmentIds);

    @Query(value = """
            SELECT date_trunc('month', process.created_at) AS monthStart,
                   COUNT(process.id) AS applicationCount
            FROM candidate_processes process
            WHERE process.company_id = :companyId
              AND process.active = true
              AND process.created_at >= :periodStart
            GROUP BY date_trunc('month', process.created_at)
            ORDER BY date_trunc('month', process.created_at)
            """, nativeQuery = true)
    List<MonthlyApplicationCountProjection> countApplicationsByMonth(
            @Param("companyId") Long companyId, @Param("periodStart") Instant periodStart);

    @Query(value = """
            SELECT date_trunc('month', process.created_at) AS monthStart,
                   COUNT(process.id) AS applicationCount
            FROM candidate_processes process
            JOIN positions position ON position.id = process.position_id
            WHERE process.company_id = :companyId
              AND process.active = true
              AND process.created_at >= :periodStart
              AND position.department_id IN (:departmentIds)
            GROUP BY date_trunc('month', process.created_at)
            ORDER BY date_trunc('month', process.created_at)
            """, nativeQuery = true)
    List<MonthlyApplicationCountProjection> countApplicationsByMonthAndDepartmentIds(
            @Param("companyId") Long companyId,
            @Param("departmentIds") Set<Long> departmentIds,
            @Param("periodStart") Instant periodStart);

    @Query("""
            SELECT department.id AS departmentId, department.name AS departmentName,
                   COUNT(process.id) AS applicationCount
            FROM CandidateProcess process
            JOIN process.position position
            JOIN position.department department
            WHERE process.company.id = :companyId AND process.active = true
            GROUP BY department.id, department.name
            ORDER BY COUNT(process.id) DESC, department.name ASC
            """)
    List<DepartmentApplicationCountProjection> countApplicationsByDepartment(
            @Param("companyId") Long companyId);

    @Query("""
            SELECT department.id AS departmentId, department.name AS departmentName,
                   COUNT(process.id) AS applicationCount
            FROM CandidateProcess process
            JOIN process.position position
            JOIN position.department department
            WHERE process.company.id = :companyId AND process.active = true
              AND department.id IN :departmentIds
            GROUP BY department.id, department.name
            ORDER BY COUNT(process.id) DESC, department.name ASC
            """)
    List<DepartmentApplicationCountProjection> countApplicationsByDepartmentIds(
            @Param("companyId") Long companyId, @Param("departmentIds") Set<Long> departmentIds);

    interface StageCandidateCountProjection {
        Long getStageId();
        String getStageName();
        String getStageCode();
        Integer getDisplayOrder();
        com.yasarbilgi.ats.pipeline.entity.PipelineStageType getStageType();
        Long getCandidateCount();
    }

    interface MonthlyApplicationCountProjection {
        Instant getMonthStart();
        Long getApplicationCount();
    }

    interface DepartmentApplicationCountProjection {
        Long getDepartmentId();
        String getDepartmentName();
        Long getApplicationCount();
    }

    Optional<CandidateProcess> findByCompanyIdAndId(
            Long companyId,
            Long candidateProcessId
    );

    // Aday sürecini aday, pozisyon, departman, pipeline ve aşama bilgileriyle getirir.
    @EntityGraph(attributePaths = {
            "candidate",
            "position",
            "position.department",
            "pipeline",
            "currentStage"
    })
    Optional<CandidateProcess> findWithDetailsByCompanyIdAndId(
            Long companyId,
            Long candidateProcessId
    );

    // Adayın aynı pozisyonda devam eden başka bir aktif süreci olup olmadığını kontrol eder.
    boolean existsByCompanyIdAndCandidateIdAndPositionIdAndActiveTrue(
            Long companyId,
            Long candidateId,
            Long positionId
    );

    // Pipeline üzerinde aktif bir aday süreci bulunup bulunmadığını kontrol eder.
    boolean existsByCompanyIdAndPipelineIdAndActiveTrue(Long companyId, Long pipelineId);

    // Belirtilen aşamada aktif bir aday süreci bulunup bulunmadığını kontrol eder.
    boolean existsByCompanyIdAndCurrentStageIdAndActiveTrue(Long companyId, Long stageId);

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
    @EntityGraph(attributePaths = {"position", "position.department", "pipeline", "currentStage"})
    List<CandidateProcess> findAllByCompanyIdAndCandidateIdAndActiveTrueOrderByCreatedAtDesc(
            Long companyId,
            Long candidateId
    );

    // Adayın yalnızca izin verilen departmanlardaki aktif süreçlerini getirir.
    @EntityGraph(attributePaths = {"position", "position.department", "pipeline", "currentStage"})
    List<CandidateProcess> findAllByCompanyIdAndCandidateIdAndPositionDepartmentIdInAndActiveTrueOrderByCreatedAtDesc(
            Long companyId, Long candidateId, Set<Long> departmentIds);

    // Adayın izin verilen departmanlardan en az birinde aktif süreci olup olmadığını kontrol eder.
    boolean existsByCompanyIdAndCandidateIdAndPositionDepartmentIdInAndActiveTrue(
            Long companyId, Long candidateId, Set<Long> departmentIds);

    // Sürecin izin verilen departmanlardan birine ait olup olmadığını kontrol eder.
    boolean existsByCompanyIdAndIdAndPositionDepartmentIdInAndActiveTrue(
            Long companyId, Long candidateProcessId, Set<Long> departmentIds);
}
