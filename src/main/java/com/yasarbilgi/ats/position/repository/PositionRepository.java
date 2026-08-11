package com.yasarbilgi.ats.position.repository;

import com.yasarbilgi.ats.position.entity.Position;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;

public interface PositionRepository extends JpaRepository<Position, Long> {

    // Şirkette belirtilen durumda bulunan aktif pozisyonların sayısını getirir.
    long countByCompanyIdAndStatusAndActiveTrue(Long companyId, PositionStatus status);

    long countByCompanyIdAndActiveTrueAndCreatedAtGreaterThanEqual(Long companyId, Instant periodStart);

    long countByCompanyIdAndDepartmentIdInAndActiveTrueAndCreatedAtGreaterThanEqual(
            Long companyId, Set<Long> departmentIds, Instant periodStart);

    // İzin verilen departmanlardaki aktif pozisyonları durumuna göre sayar.
    long countByCompanyIdAndDepartmentIdInAndStatusAndActiveTrue(
            Long companyId, Set<Long> departmentIds, PositionStatus status);

    Optional<Position> findByCompanyIdAndId(Long companyId, Long positionId);

    // Şirkete ait pozisyonu benzersiz pozisyon koduna göre getirir.
    Optional<Position> findByCompanyIdAndCode(Long companyId, String code);

    // Şirket içerisinde verilen pozisyon kodunun kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCompanyIdAndCode(Long companyId, String code);

    // Şirkete ait aktif pozisyonları durumuna göre ve başlık sırasıyla getirir.
    List<Position> findAllByCompanyIdAndStatusAndActiveTrueOrderByTitleAsc(
            Long companyId,
            PositionStatus status
    );

    // Şirketteki belirli bir departmana ait aktif pozisyonları başlık sırasıyla getirir.
    List<Position> findAllByCompanyIdAndDepartmentIdAndActiveTrueOrderByTitleAsc(
            Long companyId,
            Long departmentId
    );

    // Şirkete ait aktif pozisyonları departman bilgisiyle birlikte getirir.
    @EntityGraph(attributePaths = "department")
    List<Position> findAllByCompanyIdAndActiveTrueOrderByTitleAsc(Long companyId);

    // Şirkete ait aktif ve pasif tüm pozisyonları departman bilgisiyle birlikte getirir.
    @EntityGraph(attributePaths = "department")
    List<Position> findAllByCompanyIdOrderByTitleAsc(Long companyId);

    @EntityGraph(attributePaths = "department")
    @Query("""
            SELECT position FROM Position position
            WHERE position.company.id = :companyId AND position.active = true
              AND (:departmentId IS NULL OR position.department.id = :departmentId)
              AND (:status IS NULL OR position.status = :status)
            """)
    Page<Position> search(@Param("companyId") Long companyId,
                          @Param("departmentId") Long departmentId,
                          @Param("status") PositionStatus status,
                          Pageable pageable);

    @EntityGraph(attributePaths = "department")
    @Query("""
            SELECT position FROM Position position
            WHERE position.company.id = :companyId AND position.active = true
              AND position.department.id IN :departmentIds
              AND (:departmentId IS NULL OR position.department.id = :departmentId)
              AND (:status IS NULL OR position.status = :status)
            """)
    Page<Position> searchByDepartmentScope(@Param("companyId") Long companyId,
                                           @Param("departmentIds") Set<Long> departmentIds,
                                           @Param("departmentId") Long departmentId,
                                           @Param("status") PositionStatus status,
                                           Pageable pageable);
}
