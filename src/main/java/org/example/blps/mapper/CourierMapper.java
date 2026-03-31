package org.example.blps.mapper;
import org.example.blps.dto.requestDto.CourierRequestDto;
import org.example.blps.entity.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourierMapper {
    Courier fromDtoToEntity(CourierRequestDto CourierRequestDto);
}
