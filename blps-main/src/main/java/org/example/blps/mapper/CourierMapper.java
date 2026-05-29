package org.example.blps.mapper;

import org.example.blps.dto.responseDto.CourierResponseDto;
import org.example.blps.entity.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourierMapper {
    CourierResponseDto fromEntityToDto(Courier courier);
}
