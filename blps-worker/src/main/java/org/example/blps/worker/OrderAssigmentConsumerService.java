package org.example.blps.worker;

import org.jboss.logging.Logger;
import org.example.blps.dto.JmsMessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.example.blps.mapper.JmsMessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class OrderAssigmentConsumerService {
    private static final Logger LOG = Logger.getLogger(OrderAssigmentConsumerService.class);
    private final OrderRefreshService orderRefreshService;
    private final JmsMessageMapper jmsMessageMapper;
    private final String nodeId;

    public OrderAssigmentConsumerService(OrderRefreshService orderRefreshService,
                                         JmsMessageMapper jmsMessageMapper,
                                         @Value("${app.node-id:${jboss.node.name:${server.port:local}}}") String nodeId) {
        this.orderRefreshService = orderRefreshService;
        this.jmsMessageMapper = jmsMessageMapper;
        this.nodeId = nodeId;
    }

    @JmsListener(destination = "order.assignment.queue")
    public void listenHandle(String message){
        try{
            JmsMessageDto jmsMessageDto = jmsMessageMapper.fromJson(message);
            OrderAssignmentMessageType type = jmsMessageDto.type();
            LOG.infov("Node {0} received JMS message from {1}: {2}", nodeId, "order.assignment.queue", jmsMessageDto);
            if (type == OrderAssignmentMessageType.ASSIGN_ORDER){
                orderRefreshService.refreshWaitingOrder(jmsMessageDto.orderId());
            }
            if (type == OrderAssignmentMessageType.EXPIRE_ASSIGNMENT){
                orderRefreshService.refreshAssignedOrder(jmsMessageDto.orderAttemptId());
            }
        }catch(JmsException e){
            throw new IllegalArgumentException("Не удалось обработать");
        }
    }
}
