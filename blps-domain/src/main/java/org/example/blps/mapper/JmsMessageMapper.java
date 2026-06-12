package org.example.blps.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.blps.dto.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface JmsMessageMapper {
    ObjectMapper jsonMapper = new ObjectMapper();
    default String toJson(MessageDto message) {
        try {
            return jsonMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать сообщение для очереди", e);
        }
    }
    default MessageDto fromJson(String message) {
        try {
            return jsonMapper.readValue(message, MessageDto.class);
        }catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Не удалось десериализовать сообщение из очереди", e);
        }
    }
}
