package org.example.blps.xmp;

import org.example.blps.service.OrderProducerService;
import org.jboss.logging.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!xmpp")
public class MockXmppOrderAssignmentSenderService implements XmppOrderAssignmentSender {
    private static final Logger LOG = Logger.getLogger(MockXmppOrderAssignmentSenderService.class);
    private final OrderProducerService orderProducerService;

    public MockXmppOrderAssignmentSenderService(OrderProducerService orderProducerService) {
        this.orderProducerService = orderProducerService;
    }

    @Override
    public void send(String payload) {
        LOG.infov("XMPP is disabled, routing mocked XMPP message to JMS: {0}", payload);
        orderProducerService.publishMessage(payload);
    }
}
