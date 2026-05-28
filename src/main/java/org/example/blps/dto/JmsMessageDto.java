package org.example.blps.dto;

import org.example.blps.enums.OrderAssignmentMessageType;


public record JmsMessageDto (OrderAssignmentMessageType type, Long orderId, Long orderAttemptId) {
}
