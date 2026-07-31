package com.yasarbilgi.ats.pipeline.mapper;

import com.yasarbilgi.ats.pipeline.dto.response.PipelineStageResponseDto;
import com.yasarbilgi.ats.pipeline.dto.response.PipelineSummaryResponseDto;
import com.yasarbilgi.ats.pipeline.entity.PipelineStage;
import com.yasarbilgi.ats.pipeline.entity.RecruitmentPipeline;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PipelineMapper {

    PipelineSummaryResponseDto toSummaryResponseDto(RecruitmentPipeline pipeline);

    PipelineStageResponseDto toStageResponseDto(PipelineStage stage);
}
