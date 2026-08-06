package com.yasarbilgi.ats.interaction.mapper;

import com.yasarbilgi.ats.interaction.dto.response.InteractionResponseDto;
import com.yasarbilgi.ats.interaction.entity.Interaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InteractionMapper {

    // İletişim kaydını aday ve süreç kimlikleriyle API yanıtına dönüştürür.
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateProcessId", source = "candidateProcess.id")
    InteractionResponseDto toResponseDto(Interaction interaction);
}
