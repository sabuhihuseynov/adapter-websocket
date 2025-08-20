package org.example.adapterwebsocket.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.User;
import org.example.adapterwebsocket.model.WebSocketConnectionEstablishedEvent;
import org.example.adapterwebsocket.service.CryptoRateSubscriptionService;
import org.example.adapterwebsocket.service.SessionManagerService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final CryptoRateSubscriptionService cryptoSubscriptionService;
    private final SessionManagerService sessionManagerService;

    @EventListener
    public void handleWebSocketSessionEstablished(WebSocketConnectionEstablishedEvent event) {
        WebSocketSession session = event.getWebSocketSession();
        sessionManagerService.addWebSocketSession(session.getId(), session);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        var sessionId = headerAccessor.getSessionId();
        String userId = (headerAccessor.getUser() instanceof User user) ? user.getName() : null;

        sessionManagerService.registerSession(sessionId, userId);
        log.info("WebSocket session connected: {}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket session disconnected: {}", sessionId);
        cryptoSubscriptionService.handleSessionDisconnect(sessionId);
    }
}