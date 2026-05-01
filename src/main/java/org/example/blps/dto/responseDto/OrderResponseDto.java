package org.example.blps.dto.responseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.blps.enums.OrderStatus;

import java.time.LocalDateTime;


public record OrderResponseDto(Long id, OrderStatus status, String address, String content, @JsonFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime creationDate) {
}
