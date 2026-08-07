package com.yasarbilgi.ats.candidateprocess.mapper;

import com.yasarbilgi.ats.candidate.entity.Candidate;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateCardResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateCompensationResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessDetailResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateProcessResponseDto;
import com.yasarbilgi.ats.candidateprocess.dto.response.CandidateStageHistoryResponseDto;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcess;
import com.yasarbilgi.ats.candidateprocess.entity.CandidateProcessStageHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateProcessMapper {

    // Aday sürecini oluşturma ve aşama değiştirme yanıtına dönüştürür.
    default CandidateProcessResponseDto toResponseDto(CandidateProcess process) {
        Candidate candidate = process.getCandidate();

        return new CandidateProcessResponseDto(
                process.getId(),
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                process.getPosition().getId(),
                process.getPipeline().getId(),
                process.getCurrentStage().getId(),
                process.getCurrentStage().getName(),
                process.getCreatedAt(),
                process.getUpdatedAt()
        );
    }

    // Aday sürecini pipeline board üzerinde gösterilen kısa karta dönüştürür.
    default CandidateCardResponseDto toCardResponseDto(CandidateProcess process) {
        Candidate candidate = process.getCandidate();

        return new CandidateCardResponseDto(
                process.getId(),
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                candidate.getLinkedinUrl()
        );
    }

    // Aday sürecini maaş alanlarını içermeyen detay yanıtına dönüştürür.
    default CandidateProcessDetailResponseDto toDetailResponseDto(CandidateProcess process) {
        Candidate candidate = process.getCandidate();

        return new CandidateProcessDetailResponseDto(
                process.getId(),
                candidate.getId(),
                candidate.getFirstName() + " " + candidate.getLastName(),
                process.getPosition().getDepartment().getId(),
                process.getPosition().getDepartment().getName(),
                process.getPosition().getId(),
                process.getPosition().getTitle(),
                process.getPipeline().getId(),
                process.getPipeline().getName(),
                process.getCurrentStage().getId(),
                process.getCurrentStage().getName(),
                process.getCurrentStage().getStageType(),
                process.getCompletedAt(),
                process.isActive(),
                process.getCreatedAt(),
                process.getUpdatedAt()
        );
    }

    // Aday sürecinin hassas maaş alanlarını ayrı API yanıtına dönüştürür.
    @Mapping(target = "candidateProcessId", source = "id")
    CandidateCompensationResponseDto toCompensationResponseDto(CandidateProcess process);

    // Aşama geçmişi kaydını önceki ve sonraki aşama bilgileriyle yanıta dönüştürür.
    @Mapping(target = "fromStageId", source = "fromStage.id")
    @Mapping(target = "fromStageName", source = "fromStage.name")
    @Mapping(target = "toStageId", source = "toStage.id")
    @Mapping(target = "toStageName", source = "toStage.name")
    @Mapping(target = "changedAt", source = "createdAt")
    @Mapping(target = "changedBy", source = "createdBy")
    CandidateStageHistoryResponseDto toStageHistoryResponseDto(
            CandidateProcessStageHistory history
    );
}
