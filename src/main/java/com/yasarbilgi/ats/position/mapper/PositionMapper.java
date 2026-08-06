package com.yasarbilgi.ats.position.mapper;

import com.yasarbilgi.ats.position.dto.response.PositionSummaryResponseDto;
import com.yasarbilgi.ats.position.dto.response.PositionResponseDto;
import com.yasarbilgi.ats.position.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    // Pozisyonu seçim listelerinde kullanılan özet yanıta dönüştürür.
    PositionSummaryResponseDto toSummaryResponseDto(Position position);

    // Pozisyonu departman ve süreç ayrıntılarıyla API yanıtına dönüştürür.
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    PositionResponseDto toResponseDto(Position position);
}
