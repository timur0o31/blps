package org.example.blps.dto;

import org.example.blps.enums.OrderAssignmentMessageType;

public record MessageDto(OrderAssignmentMessageType type, Long orderId, Long orderAttemptId) {
}
