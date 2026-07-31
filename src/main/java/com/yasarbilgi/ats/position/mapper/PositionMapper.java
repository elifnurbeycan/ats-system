package com.yasarbilgi.ats.position.mapper;

import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.entity.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    PositionSummaryResponseDto toSummaryResponseDto(Position position);
}
