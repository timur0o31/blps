package org.example.blps.mapper;
import org.example.blps.dto.requestDto.ClientRequestDto;
import org.example.blps.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client fromDtoToEntity(ClientRequestDto clientRequestDto);

}
