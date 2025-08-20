package org.example.adapterwebsocket.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionManagerService {

    private static final String POD_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String POD_SESSIONS_PREFIX = "dbs.websocket.pod.sessions";
    private static final String SESSION_TO_CUSTOMER_PREFIX = "dbs.websocket.session.customer";

    private final ConcurrentHashMap<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpUserRegistry userRegistry;

    public SessionManagerService(@Qualifier("cryptoRateSubscriptionRedisTemplate")
                          RedisTemplate<String, String> redisTemplate, SimpUserRegistry userRegistry) {
        this.redisTemplate = redisTemplate;
        this.userRegistry = userRegistry;
    }

    @PostConstruct
    public void initialize() {
        log.info("Session manager initialized with POD_ID: {}", POD_ID);
    }

    public void registerSession(String sessionId, String customerId) {
        log.info("Registering session {} for customer {} on pod {}", sessionId, customerId, POD_ID);

        String podSessionsKey = getPodSessionsKey();
        redisTemplate.opsForSet().add(podSessionsKey, sessionId);

        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        redisTemplate.opsForValue().set(sessionCustomerKey, customerId);
    }

    public void unregisterSession(String sessionId) {
        log.info("Unregistering session {} from pod {}", sessionId, POD_ID);

        String podSessionsKey = getPodSessionsKey();
        redisTemplate.opsForSet().remove(podSessionsKey, sessionId);

        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        redisTemplate.delete(sessionCustomerKey);

        removeWebSocketSession(sessionId);
    }

    public void addWebSocketSession(String sessionId, WebSocketSession webSocketSession) {
        webSocketSessions.put(sessionId, webSocketSession);
        log.debug("Added WebSocket session: {}", sessionId);
    }

    public void removeWebSocketSession(String sessionId) {
        webSocketSessions.remove(sessionId);
        log.debug("Removed WebSocket session: {}", sessionId);
    }

    public void forceCloseWebSocketSession(String sessionId) {
        WebSocketSession session = webSocketSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION);
                log.info("Force closed WebSocket session: {}", sessionId);
            } catch (IOException e) {
                log.error("Failed to close WebSocket session {}: {}", sessionId, e.getMessage());
            }
            webSocketSessions.remove(sessionId);
        }
    }

    public void checkTokenExpirations() {
        Set<String> expiredSessionIds = new HashSet<>();

        userRegistry.getUsers().forEach(simpUser -> {
            if (simpUser.getPrincipal() instanceof User user && user.isTokenExpired()) {
                simpUser.getSessions().forEach(session -> expiredSessionIds.add(session.getId()));
            }
        });

        expiredSessionIds.forEach(this::forceCloseWebSocketSession);

    }

    public String getPodSessionsKey() {
        return String.format("%s.%s", POD_SESSIONS_PREFIX, POD_ID);
    }

    public String getSessionToCustomerKey(String sessionId) {
        return String.format("%s.%s", SESSION_TO_CUSTOMER_PREFIX, sessionId);
    }
}