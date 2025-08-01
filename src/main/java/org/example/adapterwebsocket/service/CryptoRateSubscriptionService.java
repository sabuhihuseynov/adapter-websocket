package org.example.adapterwebsocket.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.client.CryptoCurrencyClient;
import org.example.adapterwebsocket.client.model.RateStreamControlRequest;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoRateSubscriptionService {

    private final ConcurrentHashMap<CurrencyPair, Set<String>> pairSubscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<CurrencyPair>> sessionSubscriptions = new ConcurrentHashMap<>();
    private final CryptoCurrencyClient cryptoCurrencyClient;

    public void subscribe(String sessionId, CurrencyPair currencyPair) {
        log.info("Subscribing session {} to currency pair {}", sessionId, currencyPair);

        addSessionToPair(sessionId, currencyPair);
        addPairToSession(sessionId, currencyPair);

        log.info("Session {} subscribed to {}. Total subscribers for this pair: {}",
                sessionId, currencyPair, pairSubscriptions.get(currencyPair).size());
    }

    public void unsubscribe(String sessionId, CurrencyPair currencyPair) {
        log.info("Unsubscribing session {} from currency pair {}", sessionId, currencyPair);

        removeSessionFromPair(sessionId, currencyPair);
        removePairFromSession(sessionId, currencyPair);

        int remainingSubscribers = pairSubscriptions.getOrDefault(currencyPair, Set.of()).size();
        log.info("Session {} unsubscribed from {}. Remaining subscribers for this pair: {}",
                sessionId, currencyPair, remainingSubscribers);
    }

    public void handleSessionDisconnect(String sessionId) {
        log.info("Handling session disconnect for session {}", sessionId);

        Set<CurrencyPair> sessionPairs = sessionSubscriptions.remove(sessionId);
        if (sessionPairs == null || sessionPairs.isEmpty()) {
            log.info("No subscriptions found for disconnected session {}", sessionId);
            return;
        }

        Set<CurrencyPair> pairsToDisable = new HashSet<>();
        sessionPairs.forEach(pair -> {
            Set<String> subscribers = pairSubscriptions.get(pair);
            if (subscribers != null) {
                subscribers.remove(sessionId);
                if (subscribers.isEmpty()) {
                    pairSubscriptions.remove(pair);
                    pairsToDisable.add(pair);
                }
            }
        });
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(pairsToDisable, false));
        log.info("Cleaned up {} subscriptions for disconnected session {}", sessionPairs.size(), sessionId);
    }

    private void addSessionToPair(String sessionId, CurrencyPair currencyPair) {
        Set<String> subscribers = pairSubscriptions.computeIfAbsent(currencyPair, k -> ConcurrentHashMap.newKeySet());
        boolean wasEmpty = subscribers.isEmpty();
        subscribers.add(sessionId);

        if (wasEmpty) {
            enableRateStreaming(currencyPair);
        }
    }

    private void addPairToSession(String sessionId, CurrencyPair currencyPair) {
        sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(currencyPair);
    }

    private void removeSessionFromPair(String sessionId, CurrencyPair currencyPair) {
        Set<String> subscribers = pairSubscriptions.get(currencyPair);
        if (subscribers == null) {
            return;
        }

        subscribers.remove(sessionId);
        if (subscribers.isEmpty()) {
            pairSubscriptions.remove(currencyPair);
            disableRateStreaming(currencyPair);
        }
    }

    private void removePairFromSession(String sessionId, CurrencyPair currencyPair) {
        Set<CurrencyPair> sessionPairs = sessionSubscriptions.get(sessionId);
        if (sessionPairs != null) {
            sessionPairs.remove(currencyPair);
            if (sessionPairs.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }
    }

    private void enableRateStreaming(CurrencyPair currencyPair) {
        log.info("Starting rate streaming for currency pair {} (first subscriber)", currencyPair);
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(Set.of(currencyPair), true));
    }

    private void disableRateStreaming(CurrencyPair currencyPair) {
        log.info("Stopping rate streaming for currency pair {} (no more subscribers)", currencyPair);
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(Set.of(currencyPair), false));
    }

    public boolean hasPairSubscription(CurrencyPair currencyPair) {
        return pairSubscriptions.containsKey(currencyPair) && !pairSubscriptions.get(currencyPair).isEmpty();
    }

    @PreDestroy
    public void stopAllActiveRateStreaming() {
        log.warn("Stopping all active rate streaming...");

        try {
            Set<CurrencyPair> activePairs = pairSubscriptions.keySet();

            if (activePairs.isEmpty()) {
                log.info("No active currency pairs to stop during shutdown");
                return;
            }

            log.info("Stopping rate streaming for {} active currency pairs: {}",
                    activePairs.size(), activePairs);

            cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(activePairs, false));

            int totalSessions = sessionSubscriptions.size();
            int totalPairSubscriptions = pairSubscriptions.size();

            log.warn("Stopping all active streaming completed. Cleaned up {} sessions and {} pair subscriptions",
                    totalSessions, totalPairSubscriptions);
        } catch (Exception e) {
            log.error("Error during stopping all active streaming: {}", e.getMessage(), e);
        }
    }

}