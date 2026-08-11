package com.yasarbilgi.ats.pipeline.repository;

import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RecruitmentPipelineRepository
        extends JpaRepository<RecruitmentPipeline, Long> {

    Optional<RecruitmentPipeline> findByCompanyIdAndId(
            Long companyId,
            Long pipelineId
    );

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

    Page<RecruitmentPipeline> findAllByCompanyIdAndActiveTrue(Long companyId, Pageable pageable);

    // Şirketin aktif varsayılan pipeline'ını getirir.
    Optional<RecruitmentPipeline> findByCompanyIdAndDefaultPipelineTrueAndActiveTrue(
            Long companyId
    );
}
