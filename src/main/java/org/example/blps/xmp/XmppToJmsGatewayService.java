package org.example.blps.xmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.blps.dto.JmsMessageDto;
import org.example.blps.service.OrderProducerService;
import org.jboss.logging.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("xmpp")
public class XmppToJmsGatewayService {
    private static final Logger LOG = Logger.getLogger(XmppToJmsGatewayService.class);

    private final OrderProducerService orderProducerService;
    private final ObjectMapper objectMapper;
    private final String host;
    private final int port;
    private final String domain;
    private final String username;
    private final String password;
    private XMPPTCPConnection connection;

    public XmppToJmsGatewayService(OrderProducerService orderProducerService,
                                   ObjectMapper objectMapper,
                                   @Value("${app.xmpp.host:localhost}") String host,
                                   @Value("${app.xmpp.port:5222}") int port,
                                   @Value("${app.xmpp.domain:localhost}") String domain,
                                   @Value("${app.xmpp.gateway.username:blps-gateway}") String username,
                                   @Value("${app.xmpp.gateway.password:blps-gateway}") String password) {
        this.orderProducerService = orderProducerService;
        this.objectMapper = objectMapper;
        this.host = host;
        this.port = port;
        this.domain = domain;
        this.username = username;
        this.password = password;
    }

    @PostConstruct
    public void start() {
        try {
            connection = new XMPPTCPConnection(connectionConfiguration());
            ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
            connection.connect();
            connection.login();
            connection.addAsyncStanzaListener(stanza -> handleXmppMessage((Message) stanza),
                    new StanzaTypeFilter(Message.class));
            LOG.infov("XMPP gateway connected as {0}@{1}, routing messages to {2}",
                    username, domain, OrderProducerService.ASSIGNMENT_QUEUE);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось запустить XMPP gateway", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }
    }

    private void handleXmppMessage(Message message) {
        String body = message.getBody();
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            objectMapper.readValue(body, JmsMessageDto.class);
            orderProducerService.publishMessage(body);
            LOG.infov("Routed XMPP message from {0} to JMS queue {1}: {2}",
                    message.getFrom(), OrderProducerService.ASSIGNMENT_QUEUE, body);
        } catch (Exception e) {
            LOG.errorv(e, "Failed to route XMPP message to JMS queue: {0}", body);
        }
    }

    private XMPPTCPConnectionConfiguration connectionConfiguration() throws Exception {
        return XMPPTCPConnectionConfiguration.builder()
                .setHost(host)
                .setPort(port)
                .setXmppDomain(domain)
                .setUsernameAndPassword(username, password)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
    }
}
