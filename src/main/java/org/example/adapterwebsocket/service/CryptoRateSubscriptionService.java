package org.example.adapterwebsocket.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.client.CryptoCurrencyClient;
import org.example.adapterwebsocket.client.model.RateStreamControlRequest;
import org.example.adapterwebsocket.dao.entity.UnDisabledCryptoStreamingEntity;
import org.example.adapterwebsocket.dao.repository.UnDisabledCryptoStreamingRepository;
import org.example.adapterwebsocket.dao.repository.cache.CryptoRateSubscriptionRedisRepository;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoRateSubscriptionService {

    private final CryptoCurrencyClient cryptoCurrencyClient;
    private final CryptoRateSubscriptionRedisRepository subscriptionRedisRepository;
    private final UnDisabledCryptoStreamingRepository unDisabledCryptoStreamingRepository;
    private final SessionManager sessionManager;

    public void subscribe(String sessionId, CurrencyPair currencyPair) {
        log.info("Subscribing session {} to currency pair {}", sessionId, currencyPair);

        long subscribersBeforeAdd = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);
        if (subscribersBeforeAdd == 0) {
            log.info("First subscriber for {}. Starting rate streaming", currencyPair);
            enableRateStreaming(currencyPair);
        }

        subscriptionRedisRepository.addSessionToPair(sessionId, currencyPair);
        subscriptionRedisRepository.addPairToSession(sessionId, currencyPair);

        long totalSubscribers = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);
        log.info("Session {} subscribed to {}. Total subscribers for this pair: {}",
                sessionId, currencyPair, totalSubscribers);
    }

    public void unsubscribe(String sessionId, CurrencyPair currencyPair) {
        log.info("Unsubscribing session {} from currency pair {}", sessionId, currencyPair);

        subscriptionRedisRepository.removeSessionFromPair(sessionId, currencyPair);
        subscriptionRedisRepository.removePairFromSession(sessionId, currencyPair);

        long remainingSubscribers = subscriptionRedisRepository.getPairSubscriberCount(currencyPair);
        if (remainingSubscribers == 0) {
            log.info("No more subscribers for {}. Stopping rate streaming", currencyPair);
            disableRateStreaming(currencyPair);
        }

        log.info("Session {} unsubscribed from {}. Remaining subscribers for this pair: {}",
                sessionId, currencyPair, remainingSubscribers);
    }

    public void handleSessionDisconnect(String sessionId) {
        Set<CurrencyPair> currencyPairs = subscriptionRedisRepository.removeAllSessionSubscriptions(sessionId);
        sessionManager.unregisterSession(sessionId);

        if (currencyPairs.isEmpty()) {
            log.info("Session {} had no subscriptions", sessionId);
            return;
        }
        disableRateStreamingIfApplicable(currencyPairs);
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
            disableRateStreaming(pairsToDisable);
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
        } catch (Exception ex) {
            log.error("Failed to enable rate streaming for {}: {}", currencyPair, ex.getMessage());
            throw ex;
        }
    }

    private void disableRateStreaming(CurrencyPair currencyPair) {
        disableRateStreaming(Set.of(currencyPair));
    }

    private void disableRateStreaming(Set<CurrencyPair> currencyPairs) {
        if (currencyPairs.isEmpty()) {
            return;
        }

        try {
            cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(currencyPairs, false));
            log.info("Successfully disabled rate streaming for {} pairs", currencyPairs);
        } catch (Exception e) {
            log.error("Failed to disable rate streaming for {} pairs: {}", currencyPairs, e.getMessage());
            saveForRetry(currencyPairs);
        }
    }

    private void saveForRetry(Set<CurrencyPair> currencyPairs) {
        if (currencyPairs.isEmpty()) {
            return;
        }

        List<UnDisabledCryptoStreamingEntity> entities = currencyPairs.stream()
                .map(pair -> new UnDisabledCryptoStreamingEntity(pair.getFrom(), pair.getTo()))
                .toList();

        unDisabledCryptoStreamingRepository.saveAll(entities);
        log.info("Saved {} currency pairs for retry", currencyPairs);
    }

    public void processFailedDisables(List<UnDisabledCryptoStreamingEntity> retryDisableEntities) {
        if (retryDisableEntities.isEmpty()) {
            return;
        }

        var entitiesToRemove = findAndRemoveReactivatedEntities(retryDisableEntities);
        var remainingEntities = getRemainingEntities(retryDisableEntities, entitiesToRemove);

        if (!remainingEntities.isEmpty()) {
            attemptBulkDisableForEntities(remainingEntities);
        }
    }

    private Set<UnDisabledCryptoStreamingEntity> findAndRemoveReactivatedEntities(
            List<UnDisabledCryptoStreamingEntity> entities) {
        Set<UnDisabledCryptoStreamingEntity> entitiesToRemove = new HashSet<>();

        entities.forEach(entity -> {
            CurrencyPair pair = new CurrencyPair(entity.getCurrencyFrom(), entity.getCurrencyTo());
            long subscribers = subscriptionRedisRepository.getPairSubscriberCount(pair);

            if (subscribers > 0) {
                log.info("Pair {} has {} subscribers, marking for removal for retry", pair, subscribers);
                entitiesToRemove.add(entity);
            }
        });

        if (!entitiesToRemove.isEmpty()) {
            unDisabledCryptoStreamingRepository.deleteAll(entitiesToRemove);
        }

        return entitiesToRemove;
    }

    private List<UnDisabledCryptoStreamingEntity> getRemainingEntities(
            List<UnDisabledCryptoStreamingEntity> allEntities,
            Set<UnDisabledCryptoStreamingEntity> removedEntities) {
        return allEntities.stream()
                .filter(entity -> !removedEntities.contains(entity))
                .toList();
    }

    private void attemptBulkDisableForEntities(List<UnDisabledCryptoStreamingEntity> entities) {
        Set<CurrencyPair> pairsToDisable = entities.stream()
                .map(entity -> new CurrencyPair(entity.getCurrencyFrom(), entity.getCurrencyTo()))
                .collect(Collectors.toSet());

        try {
            performBulkDisable(pairsToDisable);
            deleteSuccessfulDisableEntities(entities);
        } catch (Exception e) {
            log.warn("Failed to disable streaming for {} pairs: {}", pairsToDisable, e.getMessage());
            handleFailedDisableEntities(entities);
        }
    }

    private void performBulkDisable(Set<CurrencyPair> pairsToDisable) {
        cryptoCurrencyClient.controlRateStreaming(new RateStreamControlRequest(pairsToDisable, false));
        log.info("Successfully disabled streaming for {} pairs", pairsToDisable.size());
    }

    private void deleteSuccessfulDisableEntities(List<UnDisabledCryptoStreamingEntity> entities) {
        unDisabledCryptoStreamingRepository.deleteAll(entities);
        log.info("Deleted {} cleanup tasks after successful bulk disable", entities.size());
    }

    private void handleFailedDisableEntities(List<UnDisabledCryptoStreamingEntity> entities) {

        entities.forEach(entity -> entity.setUpdatedAt(LocalDateTime.now()));
        unDisabledCryptoStreamingRepository.saveAll(entities);
        log.info("Updated {} tasks to retry later", entities.size());
    }

}