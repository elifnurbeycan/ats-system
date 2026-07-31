package com.yasarbilgi.ats.pipeline.service.impl;

import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;
import com.yasarbilgi.ats.pipeline.mapper.PipelineMapper;
import com.yasarbilgi.ats.pipeline.repository.PipelineStageRepository;
import com.yasarbilgi.ats.pipeline.repository.RecruitmentPipelineRepository;
import com.yasarbilgi.ats.pipeline.service.PipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

    private final RecruitmentPipelineRepository pipelineRepository;
    private final PipelineStageRepository stageRepository;
    private final PipelineMapper pipelineMapper;

    @Override
    public List<PipelineSummaryResponseDto> getPipelines(Long companyId) {
        log.debug("Fetching active pipelines for company: {}", companyId);

        return pipelineRepository.findAllByCompanyIdAndActiveTrueOrderByNameAsc(companyId)
                .stream()
                .map(pipelineMapper::toSummaryResponseDto)
                .toList();
    }

    @Override
    public List<PipelineStageResponseDto> getStages(Long companyId, Long pipelineId) {
        log.debug("Fetching stages for pipeline: {} in company: {}", pipelineId, companyId);

        return stageRepository
                .findAllByCompanyIdAndPipelineIdAndActiveTrueOrderByDisplayOrderAsc(
                        companyId,
                        pipelineId
                )
                .stream()
                .map(pipelineMapper::toStageResponseDto)
                .toList();
    }
}
