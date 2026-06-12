package org.example.blps.utils;

import org.example.blps.dto.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class StompClient {
    private final String serverUrl;
    private final WebSocketStompClient webSocketStompClient;
    private final String login;
    private final String password;
    @Autowired
    public StompClient(@Value("${blps.stomp.url}") String serverUrl,
                       @Value("${blps.stomp.login}") String login,
                               @Value("${blps.stomp.password}") String password,
                               WebSocketStompClient webSocketStompClient) {
        this.serverUrl = serverUrl;
        this.webSocketStompClient = webSocketStompClient;
        this.login = login;
        this.password = password;
    }
    public void sendMessageTask(String destination, MessageDto message) throws Exception {
        WebSocketHttpHeaders wsHeaders = new WebSocketHttpHeaders();
        wsHeaders.setSecWebSocketProtocol(List.of("v12.stomp"));
        StompHeaders headers = new StompHeaders();
        headers.setLogin(login);
        headers.setPasscode(password);
        StompSession session = webSocketStompClient
                .connectAsync(serverUrl, wsHeaders, headers, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.setDestination(destination);
        stompHeaders.setContentType(MimeTypeUtils.APPLICATION_JSON);
        session.send(stompHeaders, message);
        session.disconnect();
    }
}
