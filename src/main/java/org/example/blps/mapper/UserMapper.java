package org.example.blps.mapper;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User fromDtoToEntity(UserRequestDto userRequestDto);
}
