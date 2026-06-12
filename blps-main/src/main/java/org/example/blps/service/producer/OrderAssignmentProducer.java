package org.example.blps.service.producer;

import org.example.blps.dto.MessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.example.blps.mapper.JmsMessageMapper;
import org.example.blps.utils.StompClient;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class OrderAssignmentProducer {
    private static final Logger LOG = Logger.getLogger(OrderAssignmentProducer.class);
    private final StompClient stompClient;
    public OrderAssignmentProducer(StompClient stompClient) {
        this.stompClient = stompClient;
    }
    public void publishAssignOrder(Long orderId) {
        publish(new MessageDto(OrderAssignmentMessageType.ASSIGN_ORDER, orderId, null));
    }
    public void publishExpireAssignment(Long orderAttemptId) {
        publish(new MessageDto(OrderAssignmentMessageType.EXPIRE_ASSIGNMENT, null, orderAttemptId));
    }
    private void publish(MessageDto messageDto) {
        try {
            String destination = "order.assignment.queue";
            stompClient.sendMessageTask(destination, messageDto);
            LOG.infov("Пересылка to {0}: {1}", "order.assignment.queue", messageDto);
        }catch (Exception e){
            LOG.error("Ошибка при публикации сообщения в брокер");
        }
    }
}
