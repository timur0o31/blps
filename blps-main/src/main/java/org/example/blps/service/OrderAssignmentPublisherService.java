package org.example.blps.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.blps.dto.JmsMessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.example.blps.xmp.XmppOrderAssignmentSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderAssignmentPublisherService {
    private final OrderProducerService orderProducerService;
    private final XmppOrderAssignmentSender xmppSender;
    private final ObjectMapper objectMapper;
    private final String transport;

    public OrderAssignmentPublisherService(OrderProducerService orderProducerService, XmppOrderAssignmentSender xmppSender,
                                           ObjectMapper objectMapper, @Value("${app.assignment.transport:jms}") String transport) {
        this.orderProducerService = orderProducerService;
        this.xmppSender = xmppSender;
        this.objectMapper = objectMapper;
        this.transport = transport;
    }

    public void publishAssignOrder(Long orderId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.ASSIGN_ORDER, orderId, null));
    }

    public void publishExpireAssignment(Long orderAttemptId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.EXPIRE_ASSIGNMENT, null, orderAttemptId));
    }

    private void publish(JmsMessageDto messageDto) {
        String payload = toJson(messageDto);
        if ("xmpp".equalsIgnoreCase(transport)) {
            xmppSender.send(payload);
            return;
        }
        orderProducerService.publishMessage(payload);
    }

    private String toJson(JmsMessageDto messageDto) {
        try {
            return objectMapper.writeValueAsString(messageDto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать сообщение для очереди", e);
        }
    }
}
