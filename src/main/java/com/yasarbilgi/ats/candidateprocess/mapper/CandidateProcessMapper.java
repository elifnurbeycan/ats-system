package com.yasarbilgi.ats.candidateprocess.mapper;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateCardResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CandidateProcessMapper {

    default CandidateProcessResponseDto toResponseDto(CandidateProcess process) {
        Candidate candidate = process.getCandidate();

        return new CandidateProcessResponseDto(
                process.getId(),
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                process.getPosition().getId(),
                process.getPipeline().getId(),
                process.getCurrentStage().getId(),
                process.getCurrentStage().getName()
        );
    }

    default CandidateCardResponseDto toCardResponseDto(CandidateProcess process) {
        Candidate candidate = process.getCandidate();

        return new CandidateCardResponseDto(
                process.getId(),
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                candidate.getLinkedinUrl()
        );
    }
}
