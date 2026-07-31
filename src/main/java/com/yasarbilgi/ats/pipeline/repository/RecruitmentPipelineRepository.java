package com.yasarbilgi.ats.pipeline.repository;

import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecruitmentPipelineRepository
        extends JpaRepository<RecruitmentPipeline, Long> {

    // Şirkete ait pipeline'ı benzersiz koduna göre getirir.
    Optional<RecruitmentPipeline> findByCompanyIdAndCode(
            Long companyId,
            String code
    );

    // Şirket içerisinde verilen pipeline kodunun kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCompanyIdAndCode(
            Long companyId,
            String code
    );

    // Şirkete ait aktif pipeline'ları adlarına göre sıralayarak getirir.
    List<RecruitmentPipeline> findAllByCompanyIdAndActiveTrueOrderByNameAsc(
            Long companyId
    );

    // Şirketin aktif varsayılan pipeline'ını getirir.
    Optional<RecruitmentPipeline> findByCompanyIdAndDefaultPipelineTrueAndActiveTrue(
            Long companyId
    );
}