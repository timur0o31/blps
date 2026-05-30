package org.example.blps.service.xmpp;

import org.jboss.logging.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class XmppOrderAssignmentSenderService implements XmppOrderAssignmentSender {
    private static final Logger LOG = Logger.getLogger(XmppOrderAssignmentSenderService.class);

    private final String host;
    private final int port;
    private final String domain;
    private final String username;
    private final String password;
    private final String gatewayJid;

    public XmppOrderAssignmentSenderService(@Value("${app.xmpp.host:localhost}") String host, @Value("${app.xmpp.port:5222}") int port,
                                            @Value("${app.xmpp.domain:localhost}") String domain, @Value("${app.xmpp.sender.username:blps-sender}") String username,
                                            @Value("${app.xmpp.sender.password:blps-sender}") String password, @Value("${app.xmpp.gateway.jid:blps-gateway@localhost}") String gatewayJid) {
        this.host = host;
        this.port = port;
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.gatewayJid = gatewayJid;
    }
    @Override
    public void send(String payload) {
        XMPPTCPConnection connection = null;
        try {
            connection = new XMPPTCPConnection(connectionConfiguration());
            connection.connect();
            connection.login();
            Message message = new Message(JidCreate.entityBareFrom(gatewayJid), Message.Type.chat);
            message.setBody(payload);
            connection.sendStanza(message);
            LOG.infov("Sent XMPP message to {0}: {1}", gatewayJid, payload);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось отправить XMPP-сообщение", e);
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
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
