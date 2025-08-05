package org.example.adapterwebsocket.dao.repository.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class CryptoRateSubscriptionRedisRepository {

    private static final String BASE_KEY = "dbs.websocket.crypto.rate";
    private static final String PAIR_SUBSCRIBERS_PREFIX = String.format("%s.%s", BASE_KEY, "pair.subscribers");
    private static final String CUSTOMER_SUBSCRIPTIONS_PREFIX = String.format("%s.%s", BASE_KEY, "customer.pairs");

    private final RedisTemplate<String, String> redisTemplate;

    public CryptoRateSubscriptionRedisRepository(@Qualifier("cryptoRateSubscriptionRedisTemplate")
                                                 RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addCustomerToPair(String customerId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.info("Adding customer {} to pair {}", customerId, currencyPair);

        redisTemplate.opsForSet().add(pairKey, customerId);
    }

    public void addPairToCustomer(String customerId, CurrencyPair currencyPair) {
        String customerKey = getCustomerSubscriptionsKey(customerId);
        String pairString = currencyPair.toString();
        log.info("Adding pair {} to customer {}", pairString, customerId);

        redisTemplate.opsForSet().add(customerKey, pairString);
    }

    public void removeCustomerFromPair(String customerId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.info("Removing customer {} from pair {}", customerId, currencyPair);

        redisTemplate.opsForSet().remove(pairKey, customerId);
    }

    public void removePairFromCustomer(String customerId, CurrencyPair currencyPair) {
        String customerKey = getCustomerSubscriptionsKey(customerId);
        String pairString = currencyPair.toString();
        log.info("Removing pair {} from customer {}", pairString, customerId);

        redisTemplate.opsForSet().remove(customerKey, pairString);
    }

    public Set<CurrencyPair> getCustomerSubscriptions(String customerKey) {
        Set<String> pairStrings = redisTemplate.opsForSet().members(customerKey);

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

    public Set<CurrencyPair> removeAllCustomerSubscriptions(String customerId) {
        log.info("Removing all subscriptions for customer {}", customerId);
        String customerKey = getCustomerSubscriptionsKey(customerId);

        Set<CurrencyPair> customerPairs = getCustomerSubscriptions(customerKey);

        if (customerPairs.isEmpty()) {
            return new HashSet<>();
        }

        customerPairs.forEach(pair -> removeCustomerFromPair(customerId, pair));
        redisTemplate.delete(customerKey);

        log.info("Removed {} subscriptions for customer {}", customerPairs.size(), customerId);
        return customerPairs;
    }

    private String getPairSubscribersKey(CurrencyPair currencyPair) {
        return String.format("%s.%s", PAIR_SUBSCRIBERS_PREFIX, currencyPair.toString());
    }

    private String getCustomerSubscriptionsKey(String customerId) {
        return String.format("%s.%s", CUSTOMER_SUBSCRIPTIONS_PREFIX, customerId);
    }

}