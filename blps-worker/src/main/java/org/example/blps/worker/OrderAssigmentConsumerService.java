package org.example.blps.worker;

import org.jboss.logging.Logger;
import org.example.blps.dto.MessageDto;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.example.blps.mapper.JmsMessageMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class OrderAssigmentConsumerService {
    private static final Logger LOG = Logger.getLogger(OrderAssigmentConsumerService.class);
    private final OrderRefreshService orderRefreshService;
    private final JmsMessageMapper jmsMessageMapper;
    private final String nodeId;

    public OrderAssigmentConsumerService(OrderRefreshService orderRefreshService, JmsMessageMapper jmsMessageMapper,
                                         @Value("${app.node-id:${jboss.node.name:${server.port:local}}}") String nodeId) {
        this.orderRefreshService = orderRefreshService;
        this.jmsMessageMapper = jmsMessageMapper;
        this.nodeId = nodeId;
    }

    @JmsListener(destination = "order.assignment.queue")
    public void listenHandle(jakarta.jms.Message message){
        try{
            LOG.infov("строка jms: {0}", message);
            String body;
            if (message instanceof jakarta.jms.TextMessage textMessage) {
                body = textMessage.getText();
            } else if (message instanceof jakarta.jms.BytesMessage bytesMessage) {
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                body = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("JMS exception");
            }
            MessageDto messageDto = jmsMessageMapper.fromJson(body);
            OrderAssignmentMessageType type = messageDto.type();
            LOG.infov("Узел {0} пересылает JMS message с {1}: {2}", nodeId, "order.assignment.queue", messageDto);
            if (type == OrderAssignmentMessageType.ASSIGN_ORDER){
                orderRefreshService.refreshWaitingOrder(messageDto.orderId());
            }
            if (type == OrderAssignmentMessageType.EXPIRE_ASSIGNMENT){
                orderRefreshService.refreshAssignedOrder(messageDto.orderAttemptId());
            }
        }catch(Exception e){
            throw new IllegalArgumentException("Не удалось обработать");
        }
    }
}
