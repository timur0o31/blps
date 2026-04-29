package org.example.blps.mapper;

import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.entity.CourierRequest;
import org.example.blps.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourierRequestMapper {
    @Mapping(source = "courierRequest.id", target = "requestId")
    @Mapping(source = "courierRequest.courier.id", target = "courierId")
    @Mapping(source = "courierRequest.status", target = "status")
    @Mapping(source = "courierRequest.creationDate", target = "creationDate")
    @Mapping(source = "user.email", target = "email")
    CourierApplicationsResponseDto fromEntityToDto(CourierRequest courierRequest, User user);
}
