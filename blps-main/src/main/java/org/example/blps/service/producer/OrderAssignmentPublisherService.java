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
    private final boolean xmppEnabled;

    public OrderAssignmentPublisherService(OrderProducerService orderProducerService,
                                           XmppOrderAssignmentSender xmppSender,
                                           JmsMessageMapper jmsMessageMapper,
                                           @Value("${app.assignment.transport:jms}") String transport,
                                           @Value("${app.xmpp.enabled:false}") boolean xmppEnabled) {
        this.orderProducerService = orderProducerService;
        this.xmppSender = xmppSender;
        this.jmsMessageMapper = jmsMessageMapper;
        this.transport = transport;
        this.xmppEnabled = xmppEnabled;
    }

    public void publishAssignOrder(Long orderId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.ASSIGN_ORDER, orderId, null));
    }

    public void publishExpireAssignment(Long orderAttemptId) {
        publish(new JmsMessageDto(OrderAssignmentMessageType.EXPIRE_ASSIGNMENT, null, orderAttemptId));
    }

    private void publish(JmsMessageDto messageDto) {
        String data = jmsMessageMapper.toJson(messageDto);
        if (xmppEnabled && "xmpp".equalsIgnoreCase(transport)) {
            xmppSender.send(data);
            return;
        }
        orderProducerService.sendJmsMessage(data);
    }
}
