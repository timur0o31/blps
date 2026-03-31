package org.example.blps.mapper;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User fromDtoToEntity(UserRequestDto userRequestDto);
}
