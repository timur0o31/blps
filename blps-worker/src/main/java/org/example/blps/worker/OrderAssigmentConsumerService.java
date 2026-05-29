package org.example.blps.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.blps.messaging.OrderAssignmentQueue;
import org.jboss.logging.Logger;
import org.example.blps.dto.JmsMessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class OrderAssigmentConsumerService {
    private static final Logger LOG = Logger.getLogger(OrderAssigmentConsumerService.class);
    private final OrderAssigmentService orderAssigmentService;
    private final ObjectMapper objectMapper;
    private final String nodeId;

    public OrderAssigmentConsumerService(OrderAssigmentService orderAssigmentService,
                                         ObjectMapper objectMapper,
                                         @Value("${app.node-id:${jboss.node.name:${server.port:local}}}") String nodeId) {
        this.orderAssigmentService = orderAssigmentService;
        this.objectMapper = objectMapper;
        this.nodeId = nodeId;
    }

    @PostConstruct
    public void init() {
        LOG.infov("JMS consumer initialized on node {0}, listening queue {1}", nodeId, OrderAssignmentQueue.NAME);
    }

    @JmsListener(destination = OrderAssignmentQueue.NAME)
    public void handle(String message){
        try{
            JmsMessageDto dto = objectMapper.readValue(message, JmsMessageDto.class);
            OrderAssignmentMessageType type = dto.type();
            LOG.infov("Node {0} received JMS message from {1}: {2}", nodeId, OrderAssignmentQueue.NAME, dto);
            if (type == OrderAssignmentMessageType.ASSIGN_ORDER){
                orderAssigmentService.refreshWaitingOrder(dto.orderId());
            }
            if (type == OrderAssignmentMessageType.EXPIRE_ASSIGNMENT){
                orderAssigmentService.refreshAssignedOrder(dto.orderAttemptId());
            }
            LOG.infov("Node {0} processed JMS message type {1}", nodeId, type);
        }catch(JmsException e){
            throw new IllegalArgumentException("Не удалось обработать");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
