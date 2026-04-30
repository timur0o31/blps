package org.example.blps.dto.responseDto;

import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierStatus;

public record CourierResponseDto(Long id, CourierAccountState accountState, CourierStatus status, Long userId, Long deletedByAdminId) {
}
