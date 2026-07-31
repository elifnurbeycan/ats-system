package com.yasarbilgi.ats.pipeline.service;

import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;

import java.util.List;

public interface PipelineService {

    List<PipelineSummaryResponseDto> getPipelines(Long companyId);

    List<PipelineStageResponseDto> getStages(Long companyId, Long pipelineId);
}
