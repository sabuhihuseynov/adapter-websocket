package org.example.adapterwebsocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PodSessionManager {

    private final RedisTemplate<String, String> cryptoRateSubscriptionRedisTemplate;

    public static final String POD_ID = UUID.randomUUID().toString();
    private static final String POD_SESSIONS_PREFIX = "dbs.websocket.pod.sessions";
    private static final String SESSION_TO_CUSTOMER_PREFIX = "dbs.websocket.session.customer";

    @PostConstruct
    public void initialize() {
        log.info("Pod session manager initialized with ID: {}", POD_ID);
    }

    public void registerSession(String sessionId, String customerId) {
        log.debug("Registering session {} for customer {} on pod {}", sessionId, customerId, POD_ID);

        String podSessionsKey = getPodSessionsKey();
        cryptoRateSubscriptionRedisTemplate.opsForSet().add(podSessionsKey, sessionId);

        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        cryptoRateSubscriptionRedisTemplate.opsForValue().set(sessionCustomerKey, customerId);
    }

    public void unregisterSession(String sessionId) {
        log.debug("Unregistering session {} from pod {}", sessionId, POD_ID);

        String podSessionsKey = getPodSessionsKey();
        cryptoRateSubscriptionRedisTemplate.opsForSet().remove(podSessionsKey, sessionId);

        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        cryptoRateSubscriptionRedisTemplate.delete(sessionCustomerKey);
    }

    public String getCustomerBySession(String sessionId) {
        String sessionCustomerKey = getSessionToCustomerKey(sessionId);
        return cryptoRateSubscriptionRedisTemplate.opsForValue().get(sessionCustomerKey);
    }

    public Set<String> getActiveSessionsForPod() {
        String podSessionsKey = getPodSessionsKey();
        Set<String> sessions = cryptoRateSubscriptionRedisTemplate.opsForSet().members(podSessionsKey);
        return sessions != null ? sessions : new HashSet<>();
    }

    public void removePodSessions() {
        String podSessionsKey = getPodSessionsKey();
        cryptoRateSubscriptionRedisTemplate.delete(podSessionsKey);
    }

    public String getPodSessionsKey() {
        return String.format("%s.%s", POD_SESSIONS_PREFIX, POD_ID);
    }

    public String getSessionToCustomerKey(String sessionId) {
        return String.format("%s.%s", SESSION_TO_CUSTOMER_PREFIX, sessionId);
    }
}