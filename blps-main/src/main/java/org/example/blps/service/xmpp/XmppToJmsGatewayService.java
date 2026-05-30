package org.example.blps.service.xmpp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.blps.mapper.JmsMessageMapper;
import org.example.blps.service.producer.OrderProducerService;
import org.jboss.logging.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.xmpp.enabled", havingValue = "true")
public class XmppToJmsGatewayService {
    private static final Logger LOG = Logger.getLogger(XmppToJmsGatewayService.class);

    private final OrderProducerService orderProducerService;
    private final JmsMessageMapper jmsMessageMapper;
    private final String host;
    private final int port;
    private final String domain;
    private final String username;
    private final String password;
    private XMPPTCPConnection connection;

    public XmppToJmsGatewayService(OrderProducerService orderProducerService, JmsMessageMapper jmsMessageMapper,
                                   @Value("${app.xmpp.host:localhost}") String host, @Value("${app.xmpp.port:5222}") int port,
                                   @Value("${app.xmpp.domain:localhost}") String domain, @Value("${app.xmpp.gateway.username:blps-gateway}") String username,
                                   @Value("${app.xmpp.gateway.password:blps-gateway}") String password) {
        this.orderProducerService = orderProducerService;
        this.jmsMessageMapper = jmsMessageMapper;
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
                    username, domain, "order.assignment.queue");
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
            jmsMessageMapper.fromJson(body);
            orderProducerService.sendJmsMessage(body);
            LOG.infov("Routed XMPP message from {0} to JMS queue {1}: {2}",
                    message.getFrom(), "order.assignment.queue", body);
        } catch (Exception e) {
            LOG.errorv(e, "Ошибка передачи сообщения в JMS queue: {0}", body);
        }
    }
    private XMPPTCPConnectionConfiguration connectionConfiguration() throws Exception {
        return XMPPTCPConnectionConfiguration.builder()
                .setHost(host).setPort(port)
                .setXmppDomain(domain).setUsernameAndPassword(username, password)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).build();
    }
}
