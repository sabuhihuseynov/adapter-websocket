package org.example.adapterwebsocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.client.CryptoCurrencyClient;
import org.example.adapterwebsocket.client.model.RateStreamControlRequest;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.stereotype.Service;
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

        pairSubscriptions.computeIfAbsent(currencyPair, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(currencyPair);

        if (pairSubscriptions.size() == 1) {
//            startRateStreaming(currencyPair);
        }

        log.info("Session {} subscribed to {}. Total subscribers for this pair: {}",
                sessionId, currencyPair, pairSubscriptions.get(currencyPair).size());
    }

    public void unsubscribe(String sessionId, CurrencyPair currencyPair) {
        log.info("Unsubscribing session {} from currency pair {}", sessionId, currencyPair);

        Set<String> subscribers = pairSubscriptions.get(currencyPair);

        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                pairSubscriptions.remove(currencyPair);
//                stopRateStreaming(currencyPair);
            }
        }

        Set<CurrencyPair> sessionPairs = sessionSubscriptions.get(sessionId);
        if (sessionPairs != null) {
            sessionPairs.remove(currencyPair);
            if (sessionPairs.isEmpty()) {
                sessionSubscriptions.remove(sessionId);
            }
        }

        int remainingSubscribers = subscribers != null ? subscribers.size() : 0;
        log.info("Session {} unsubscribed from {}. Remaining subscribers for this pair: {}",
                sessionId, currencyPair, remainingSubscribers);
    }

    private void stopRateStreaming(CurrencyPair currencyPair) {
        log.info("Stopping rate streaming for currency pair {} (no more subscribers)", currencyPair);
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(currencyPair, false));
    }

    public void startRateStreaming(CurrencyPair currencyPair) {
        log.info("Starting rate streaming for currency pair {} (first subscriber)", currencyPair);
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(currencyPair, true));
    }

    public void handleSessionDisconnect(String sessionId) {
        log.info("Handling session disconnect for session {}", sessionId);

        Set<CurrencyPair> sessionPairs = sessionSubscriptions.remove(sessionId);
        if (sessionPairs == null || sessionPairs.isEmpty()) {
            log.info("No subscriptions found for disconnected session {}", sessionId);
            return;
        }
        sessionPairs.forEach(pair -> unsubscribe(sessionId, pair));
        log.info("Cleaned up {} subscriptions for disconnected session {}", sessionPairs.size(), sessionId);
    }
}