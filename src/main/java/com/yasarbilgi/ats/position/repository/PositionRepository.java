package com.yasarbilgi.ats.position.repository;

import com.yasarbilgi.ats.position.entity.Position;
import com.yasarbilgi.ats.position.entity.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

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
}
