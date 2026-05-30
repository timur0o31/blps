package org.example.blps.service.producer;

import org.example.blps.dto.JmsMessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.example.blps.mapper.JmsMessageMapper;
import org.example.blps.service.xmpp.XmppOrderAssignmentSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderAssignmentPublisherService {
    private final OrderProducerService orderProducerService;
    private final XmppOrderAssignmentSender xmppSender;
    private final JmsMessageMapper jmsMessageMapper;
    private final String transport;

    public OrderAssignmentPublisherService(OrderProducerService orderProducerService,
                                           XmppOrderAssignmentSender xmppSender,
                                           JmsMessageMapper jmsMessageMapper,
                                           @Value("${app.assignment.transport:jms}") String transport) {
        this.orderProducerService = orderProducerService;
        this.xmppSender = xmppSender;
        this.jmsMessageMapper = jmsMessageMapper;
        this.transport = transport;
    }

    public void publishAssignOrder(Long orderId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.ASSIGN_ORDER, orderId, null));
    }

    public void publishExpireAssignment(Long orderAttemptId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.EXPIRE_ASSIGNMENT, null, orderAttemptId));
    }

    private void publish(JmsMessageDto messageDto) {
        String data = jmsMessageMapper.toJson(messageDto);
        if ("xmpp".equalsIgnoreCase(transport)) {
            xmppSender.send(data);
            return;
        }
        orderProducerService.sendJmsMessage(data);
    }
}
