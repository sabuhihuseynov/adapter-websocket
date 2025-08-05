package org.example.adapterwebsocket.service;

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
    private final CryptoRateSubscriptionRedisRepository subscriptionRedisRepository;
    private final SessionManager sessionManager;

    public void subscribe(String customerId, CurrencyPair currencyPair) {
        log.info("Subscribing customer {} to currency pair {}", customerId, currencyPair);

        long subscribersBeforeAdd = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);

        if (subscribersBeforeAdd == 0) {
            log.info("First subscriber for {}. Starting rate streaming", currencyPair);
            enableRateStreaming(currencyPair);
        }

        subscriptionRedisRepository.addCustomerToPair(customerId, currencyPair);

        subscriptionRedisRepository.addPairToCustomer(customerId, currencyPair);

        long totalSubscribers = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);
        log.info("Customer {} subscribed to {}. Total subscribers for this pair: {}",
                customerId, currencyPair, totalSubscribers);
    }

    public void unsubscribe(String customerId, CurrencyPair currencyPair) {
        log.info("Unsubscribing customer {} from currency pair {}", customerId, currencyPair);

        subscriptionRedisRepository.removeCustomerFromPair(customerId, currencyPair);

        subscriptionRedisRepository.removePairFromCustomer(customerId, currencyPair);

        long remainingSubscribers = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);

        if (remainingSubscribers == 0) {
            log.info("No more subscribers for {}. Stopping rate streaming", currencyPair);
            disableRateStreaming(currencyPair);
        }

        log.info("Customer {} unsubscribed from {}. Remaining subscribers for this pair: {}",
                customerId, currencyPair, remainingSubscribers);
    }

    public void handleSessionDisconnect(String sessionId) {
        var customerId = sessionManager.getCustomerBySession(sessionId);
        if (customerId != null) {
            handleCustomerDisconnect(customerId);
            sessionManager.unregisterSession(sessionId);
        }
    }

    public void handleCustomerDisconnect(String customerId) {
        log.info("Handling disconnect for customer {}", customerId);

        Set<CurrencyPair> customerPairs = subscriptionRedisRepository.removeAllCustomerSubscriptions(customerId);

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
            long remainingSubscribers = subscriptionRedisRepository.getPairSubscriberCount(pair);
            if (remainingSubscribers == 0) {
                pairsToDisable.add(pair);
            }
        });

        if (!pairsToDisable.isEmpty()) {
            try {
                log.info("Stopping rate streaming for {} ", pairsToDisable);
                cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(pairsToDisable, false));
            } catch (Exception ex) {
                log.error("Failed to disable batch rate streaming for {}: {}", pairsToDisable, ex.getMessage());
            }
        }
    }

    public boolean hasPairSubscription(CurrencyPair currencyPair) {
        boolean hasActiveSubscriptions = subscriptionRedisRepository.hasPairSubscribers(currencyPair);

        log.info("Subscription check for {}: {}", currencyPair, hasActiveSubscriptions);

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

}