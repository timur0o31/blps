package org.example.blps.mapper;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    Order fromDtoToEntity(OrderRequestDto orderRequestDto);
}
