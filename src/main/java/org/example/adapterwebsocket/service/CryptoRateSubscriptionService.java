package org.example.adapterwebsocket.service;

import static org.example.adapterwebsocket.service.PodSessionManager.POD_ID;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;
import org.example.adapterwebsocket.client.CryptoCurrencyClient;
import org.example.adapterwebsocket.client.model.RateStreamControlRequest;
import org.example.adapterwebsocket.dao.repository.cache.CryptoRateSubscriptionRedisRepository;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoRateSubscriptionService {

    private final CryptoCurrencyClient cryptoCurrencyClient;
    private final CryptoRateSubscriptionRedisRepository cryptoRateSubscriptionRedisRepository;
    private final PodSessionManager podSessionManager;

    public void subscribe(String customerId, CurrencyPair currencyPair) {
        log.info("Subscribing customer {} to currency pair {}", customerId, currencyPair);

        long subscribersBeforeAdd = cryptoRateSubscriptionRedisRepository.getPairSubscriberCount(currencyPair);

        if (subscribersBeforeAdd == 0) {
            log.info("First subscriber for {}. Starting rate streaming", currencyPair);
            enableRateStreaming(currencyPair);
        }

        cryptoRateSubscriptionRedisRepository.addCustomerToPair(customerId, currencyPair);

        cryptoRateSubscriptionRedisRepository.addPairToCustomer(customerId, currencyPair);

        long totalSubscribers = cryptoRateSubscriptionRedisRepository.getPairSubscriberCount(currencyPair);
        log.info("Customer {} subscribed to {}. Total subscribers for this pair: {}",
                customerId, currencyPair, totalSubscribers);
    }

    public void unsubscribe(String customerId, CurrencyPair currencyPair) {
        log.info("Unsubscribing customer {} from currency pair {}", customerId, currencyPair);

        cryptoRateSubscriptionRedisRepository.removeCustomerFromPair(customerId, currencyPair);

        cryptoRateSubscriptionRedisRepository.removePairFromCustomer(customerId, currencyPair);

        long remainingSubscribers = cryptoRateSubscriptionRedisRepository.getPairSubscriberCount(currencyPair);

        if (remainingSubscribers == 0) {
            log.info("No more subscribers for {}. Stopping rate streaming", currencyPair);
            disableRateStreaming(currencyPair);
        }

        log.info("Customer {} unsubscribed from {}. Remaining subscribers for this pair: {}",
                customerId, currencyPair, remainingSubscribers);
    }

    public void handleSessionDisconnect(String sessionId) {
        var customerId = podSessionManager.getCustomerBySession(sessionId);
        if (customerId != null) {
            handleCustomerDisconnect(customerId);
            podSessionManager.unregisterSession(sessionId);
        }
    }

    public void handleCustomerDisconnect(String customerId) {
        log.info("Handling disconnect for customer {}", customerId);

        Set<CurrencyPair> customerPairs = cryptoRateSubscriptionRedisRepository
                .removeAllCustomerSubscriptions(customerId);

        if (customerPairs.isEmpty()) {
            log.info("Customer {} had no subscriptions", customerId);
            return;
        }
        disableRateStreamingIfApplicable(customerPairs);

        log.info("Cleaned up customer {} - removed {} subscriptions",
                customerId, customerPairs.size());
    }

    private void disableRateStreamingIfApplicable(Set<CurrencyPair> currencyPairs) {
        Set<CurrencyPair> pairsToDisable = new HashSet<>();

        currencyPairs.forEach(pair -> {
            long remainingSubscribers = cryptoRateSubscriptionRedisRepository.getPairSubscriberCount(pair);
            if (remainingSubscribers == 0) {
                pairsToDisable.add(pair);
                log.info("Pair {} now has no subscribers", pair);
            }
        });

        if (!pairsToDisable.isEmpty()) {
            log.info("Stopping rate streaming for {} pairs", pairsToDisable.size());
            cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(pairsToDisable, false));
        }
    }

    public boolean hasPairSubscription(CurrencyPair currencyPair) {
        boolean hasActiveSubscriptions = cryptoRateSubscriptionRedisRepository.hasPairSubscribers(currencyPair);

        log.debug("Subscription check for {}: {}", currencyPair, hasActiveSubscriptions);

        return hasActiveSubscriptions;
    }

    private void enableRateStreaming(CurrencyPair currencyPair) {
        try {
            var request = new RateStreamControlRequest(Set.of(currencyPair), true);
            cryptoCurrencyClient.controlRateStreaming(request);
            log.info("Successfully enabled rate streaming for {}", currencyPair);
        } catch (Exception e) {
            log.error("Failed to enable rate streaming for {}: {}", currencyPair, e.getMessage());
        }
    }

    private void disableRateStreaming(CurrencyPair currencyPair) {
        try {
            var request = new RateStreamControlRequest(Set.of(currencyPair), false);
            cryptoCurrencyClient.controlRateStreaming(request);
            log.info("Successfully disabled rate streaming for {}", currencyPair);
        } catch (Exception e) {
            log.error("Failed to disable rate streaming for {}: {}", currencyPair, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("Pod {} shutting down - cleaning up sessions (crypto rate subscription handling)", POD_ID);
        cleanupPodSessions();
    }

    private void cleanupPodSessions() {
        Set<String> sessions = podSessionManager.getActiveSessionsForPod();

        if (sessions != null && !sessions.isEmpty()) {
            log.info("Cleaning up {} sessions for pod {}", sessions.size(), POD_ID);

            sessions.forEach(this::handleSessionDisconnect);
            podSessionManager.getActiveSessionsForPod();
            podSessionManager.removePodSessions();
        }
    }
}