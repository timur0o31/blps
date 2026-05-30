package org.example.blps.service.producer;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

@Service
public class OrderProducerService {
    private static final Logger LOG = Logger.getLogger(OrderProducerService.class);
    private final ConnectionFactory connectionFactory;
    public OrderProducerService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    public void sendJmsMessage(String message) {
        try (JMSContext context = connectionFactory.createContext()) {
            Queue queue = context.createQueue("order.assignment.queue");
            context.createProducer().send(queue, message);
            LOG.infov("Sent JMS message to {0}: {1}", "order.assignment.queue", message);
        }
    }
}
