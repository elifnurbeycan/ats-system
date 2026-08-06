package com.yasarbilgi.ats.candidatenote.mapper;

import com.yasarbilgi.ats.candidatenote.dto.response.CandidateNoteResponseDto;
import com.yasarbilgi.ats.candidatenote.entity.CandidateNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateNoteMapper {

    // Aday notunu aday ve süreç kimlikleriyle API yanıtına dönüştürür.
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateProcessId", source = "candidateProcess.id")
    CandidateNoteResponseDto toResponseDto(CandidateNote note);
}
