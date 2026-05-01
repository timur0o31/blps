package org.example.blps.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.blps.enums.CourierRequestStatus;

import java.time.LocalDateTime;

public record CourierApplicationsResponseDto(Long requestId, Long courierId, String email, CourierRequestStatus status, @JsonFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime creationDate) {
}
