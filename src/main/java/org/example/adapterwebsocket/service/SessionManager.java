package org.example.adapterwebsocket.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class SessionManager {

    public static final String POD_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String POD_SESSIONS_PREFIX = "dbs.websocket.pod.sessions";
    private static final String SESSION_TO_CUSTOMER_PREFIX = "dbs.websocket.session.customer";

    private final RedisTemplate<String, String> redisTemplate;

    public SessionManager(@Qualifier("cryptoRateSubscriptionRedisTemplate")
                          RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initialize() {
        log.info("Pod session manager initialized with ID: {}", POD_ID);
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
    }

    public String getCustomerBySession(String sessionId) {
        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        return redisTemplate.opsForValue().get(sessionCustomerKey);
    }

    public String getPodSessionsKey() {
        return String.format("%s.%s", POD_SESSIONS_PREFIX, POD_ID);
    }

    public String getSessionToCustomerKey(String sessionId) {
        return String.format("%s.%s", SESSION_TO_CUSTOMER_PREFIX, sessionId);
    }
}