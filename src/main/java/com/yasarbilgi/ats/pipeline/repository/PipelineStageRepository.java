package com.yasarbilgi.ats.pipeline.repository;

import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import com.yasarbilgi.ats.pipeline.entity.PipelineStageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineStageRepository
        extends JpaRepository<PipelineStage, Long> {

    Optional<PipelineStage> findByCompanyIdAndPipelineIdAndId(
            Long companyId,
            Long pipelineId,
            Long stageId
    );

    Optional<PipelineStage>
    findFirstByCompanyIdAndPipelineIdAndStageTypeAndActiveTrueOrderByDisplayOrderAsc(
            Long companyId,
            Long pipelineId,
            PipelineStageType stageType
    );

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

    // Pipeline içindeki aktif ve pasif tüm aşamaları sıralı olarak getirir.
    List<PipelineStage> findAllByCompanyIdAndPipelineIdOrderByDisplayOrderAsc(
            Long companyId, Long pipelineId);
}
