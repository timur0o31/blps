package org.example.blps.service;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import org.example.blps.enums.OrderAssignmentMessageType;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class OrderProducerService {
    private static final Logger LOG = Logger.getLogger(OrderProducerService.class);

    private final ConnectionFactory connectionFactory;
    public static final String ASSIGNMENT_QUEUE = "order.assignment.queue";

    public OrderProducerService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void publishMessage(String message) {
        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue(ASSIGNMENT_QUEUE);
            context.createProducer().send(queue, message);
            LOG.infov("Sent JMS message to {0}: {1}", ASSIGNMENT_QUEUE, message);
        }
    }

    public void publishAssignOrder(Long orderId) {
        String message = "{\"type\":\"" + OrderAssignmentMessageType.ASSIGN_ORDER + "\",\"orderId\":" + orderId + "}";
        publishMessage(message);
    }

    public void publishExpireAssignment(Long orderAttemptId) {
        String message = "{\"type\":\"" + OrderAssignmentMessageType.EXPIRE_ASSIGNMENT
                + "\",\"orderAttemptId\":" + orderAttemptId + "}";
        publishMessage(message);
    }
}
