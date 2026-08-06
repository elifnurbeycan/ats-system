package com.yasarbilgi.ats.pipeline.service;

import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineDetailResponseDto;
import com.yasarbilgi.ats.pipeline.dto.request.*;

import java.util.List;

public interface PipelineService {

    // Şirkete aşamalarıyla birlikte yeni pipeline ekler.
    PipelineDetailResponseDto create(Long companyId, CreatePipelineRequestDto request);

    List<PipelineSummaryResponseDto> getPipelines(Long companyId);

    List<PipelineStageResponseDto> getStages(Long companyId, Long pipelineId);

    // Pipeline ayrıntısını tüm aşamalarıyla getirir.
    PipelineDetailResponseDto getById(Long companyId, Long pipelineId);

    // Pipeline bilgilerini ve varsayılan olma durumunu günceller.
    PipelineDetailResponseDto update(Long companyId, Long pipelineId, UpdatePipelineRequestDto request);

    // Aktif süreçte kullanılmayan pipeline'ı pasifleştirir.
    PipelineDetailResponseDto deactivate(Long companyId, Long pipelineId);

    // Pipeline'a yeni bir aşama ekler.
    PipelineStageResponseDto addStage(Long companyId, Long pipelineId, CreatePipelineStageRequestDto request);

    // Pipeline aşamasının görünen bilgilerini ve davranış türünü günceller.
    PipelineStageResponseDto updateStage(Long companyId, Long pipelineId, Long stageId,
                                         UpdatePipelineStageRequestDto request);

    // Pipeline aşamalarını gönderilen kimlik sırasına göre yeniden sıralar.
    List<PipelineStageResponseDto> reorderStages(Long companyId, Long pipelineId,
                                                 ReorderPipelineStagesRequestDto request);

    // Aktif aday sürecinde kullanılmayan aşamayı pasifleştirir.
    PipelineStageResponseDto deactivateStage(Long companyId, Long pipelineId, Long stageId);
}
