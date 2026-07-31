package com.yasarbilgi.ats.pipeline.repository;

import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineStageRepository
        extends JpaRepository<PipelineStage, Long> {

    // Şirkete ve pipeline'a ait aşamayı benzersiz koduna göre getirir.
    Optional<PipelineStage> findByCompanyIdAndPipelineIdAndCode(
            Long companyId,
            Long pipelineId,
            String code
    );

    // Pipeline içerisinde verilen aşama kodunun kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCompanyIdAndPipelineIdAndCode(
            Long companyId,
            Long pipelineId,
            String code
    );

    // Pipeline içerisindeki sıralama değerinin kullanılıp kullanılmadığını kontrol eder.
    boolean existsByCompanyIdAndPipelineIdAndDisplayOrder(
            Long companyId,
            Long pipelineId,
            Integer displayOrder
    );

    // Pipeline'ın aktif aşamalarını gösterim sırasına göre getirir.
    List<PipelineStage> findAllByCompanyIdAndPipelineIdAndActiveTrueOrderByDisplayOrderAsc(
            Long companyId,
            Long pipelineId
    );
}