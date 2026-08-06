package com.yasarbilgi.ats.candidate.mapper;

import com.yasarbilgi.ats.candidate.dto.response.CandidateProcessSummaryResponseDto;
import com.yasarbilgi.ats.candidate.dto.response.CandidateResponseDto;
import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    // Aday entity'sini profil API yanıtına dönüştürür.
    @Mapping(
            target = "fullName",
            expression = "java(candidate.getFirstName() + \" \" + candidate.getLastName())"
    )
    CandidateResponseDto toResponseDto(Candidate candidate);

    // Aday sürecini maaş bilgisi içermeyen özet yanıta dönüştürür.
    @Mapping(target = "positionId", source = "position.id")
    @Mapping(target = "positionTitle", source = "position.title")
    @Mapping(target = "pipelineId", source = "pipeline.id")
    @Mapping(target = "pipelineName", source = "pipeline.name")
    @Mapping(target = "currentStageId", source = "currentStage.id")
    @Mapping(target = "currentStageName", source = "currentStage.name")
    CandidateProcessSummaryResponseDto toProcessSummaryResponseDto(CandidateProcess process);
}
