package org.example.adapterwebsocket.dao.repository.cache;

import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class CryptoRateSubscriptionRedisRepository {

    private static final String BASE_KEY = "dbs.websocket.crypto.rate";
    private static final String PAIR_SUBSCRIBERS_PREFIX = String.format("%s.%s", BASE_KEY, "pair.subscribers");
    private static final String SESSION_SUBSCRIPTIONS_PREFIX = String.format("%s.%s", BASE_KEY, "session.pairs");

    private final RedisTemplate<String, String> redisTemplate;

    public CryptoRateSubscriptionRedisRepository(@Qualifier("cryptoRateSubscriptionRedisTemplate")
                                                 RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addSessionToPair(String sessionId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.debug("Adding session {} to pair {}", sessionId, currencyPair);

        redisTemplate.opsForSet().add(pairKey, sessionId);
    }

    public void addPairToSession(String sessionId, CurrencyPair currencyPair) {
        String sessionKey = getSessionSubscriptionsKey(sessionId);
        String pairString = currencyPair.toString();
        log.debug("Adding pair {} to session {}", pairString, sessionId);

        redisTemplate.opsForSet().add(sessionKey, pairString);
    }

    public void removeSessionFromPair(String sessionId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.debug("Removing session {} from pair {}", sessionId, currencyPair);

        redisTemplate.opsForSet().remove(pairKey, sessionId);
    }

    public void removePairFromSession(String sessionId, CurrencyPair currencyPair) {
        String sessionKey = getSessionSubscriptionsKey(sessionId);
        String pairString = currencyPair.toString();
        log.debug("Removing pair {} from session {}", pairString, sessionId);

        redisTemplate.opsForSet().remove(sessionKey, pairString);
    }

    public Set<CurrencyPair> getSessionSubscriptions(String sessionKey) {
        Set<String> pairStrings = redisTemplate.opsForSet().members(sessionKey);

        if (pairStrings == null || pairStrings.isEmpty()) {
            return new HashSet<>();
        }

        return pairStrings.stream()
                .map(CurrencyPair::fromString)
                .collect(Collectors.toSet());
    }

    public boolean hasPairSubscribers(CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        Long size = redisTemplate.opsForSet().size(pairKey);
        return size != null && size > 0;
    }

    public long getPairSubscriberCount(CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        Long size = redisTemplate.opsForSet().size(pairKey);
        return size != null ? size : 0;
    }

    public Set<CurrencyPair> removeAllSessionSubscriptions(String sessionId) {
        log.debug("Removing all subscriptions for session {}", sessionId);

        String sessionKey = getSessionSubscriptionsKey(sessionId);
        Set<CurrencyPair> currencyPairs = getSessionSubscriptions(sessionKey);

        if (currencyPairs.isEmpty()) {
            return new HashSet<>();
        }

        currencyPairs.forEach(pair -> removeSessionFromPair(sessionId, pair));
        redisTemplate.delete(sessionKey);

        log.debug("Removed {} subscriptions for session {}", currencyPairs.size(), sessionId);
        return currencyPairs;
    }

    private String getPairSubscribersKey(CurrencyPair currencyPair) {
        return String.format("%s.%s", PAIR_SUBSCRIBERS_PREFIX, currencyPair.toString());
    }

    private String getSessionSubscriptionsKey(String sessionId) {
        return String.format("%s.%s", SESSION_SUBSCRIPTIONS_PREFIX, sessionId);
    }

}