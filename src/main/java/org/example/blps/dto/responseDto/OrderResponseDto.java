package org.example.blps.dto.responseDto;
import org.example.blps.enums.OrderStatus;


public record OrderResponseDto(Integer id, OrderStatus status) {

}
