package org.example.adapterwebsocket.dao.repository.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CryptoRateSubscriptionRedisRepository {

    private final RedisTemplate<String, String> cryptoRateSubscriptionRedisTemplate;

    private static final String BASE_KEY = "dbs.websocket.crypto.rate";
    private static final String PAIR_SUBSCRIBERS_PREFIX = String.format("%s.%s", BASE_KEY, "pair.subscribers");
    private static final String CUSTOMER_SUBSCRIPTIONS_PREFIX = String.format("%s.%s", BASE_KEY, "customer.pairs");

    public void addCustomerToPair(String customerId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.debug("Adding customer {} to pair {}", customerId, currencyPair);

        cryptoRateSubscriptionRedisTemplate.opsForSet().add(pairKey, customerId);
    }

    public void addPairToCustomer(String customerId, CurrencyPair currencyPair) {
        String customerKey = getCustomerSubscriptionsKey(customerId);
        String pairString = currencyPair.toString();
        log.debug("Adding pair {} to customer {}", pairString, customerId);

        cryptoRateSubscriptionRedisTemplate.opsForSet().add(customerKey, pairString);
    }

    public void removeCustomerFromPair(String customerId, CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        log.debug("Removing customer {} from pair {}", customerId, currencyPair);

        cryptoRateSubscriptionRedisTemplate.opsForSet().remove(pairKey, customerId);
    }

    public void removePairFromCustomer(String customerId, CurrencyPair currencyPair) {
        String customerKey = getCustomerSubscriptionsKey(customerId);
        String pairString = currencyPair.toString();
        log.debug("Removing pair {} from customer {}", pairString, customerId);

        cryptoRateSubscriptionRedisTemplate.opsForSet().remove(customerKey, pairString);
    }

    public Set<CurrencyPair> getCustomerSubscriptions(String customerKey) {
        Set<String> pairStrings = cryptoRateSubscriptionRedisTemplate.opsForSet().members(customerKey);

        if (pairStrings == null || pairStrings.isEmpty()) {
            return new HashSet<>();
        }

        return pairStrings.stream()
                .map(CurrencyPair::fromString)
                .collect(Collectors.toSet());
    }

    public boolean hasPairSubscribers(CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        Long size = cryptoRateSubscriptionRedisTemplate.opsForSet().size(pairKey);
        return size != null && size > 0;
    }

    public long getPairSubscriberCount(CurrencyPair currencyPair) {
        String pairKey = getPairSubscribersKey(currencyPair);
        Long size = cryptoRateSubscriptionRedisTemplate.opsForSet().size(pairKey);
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
        cryptoRateSubscriptionRedisTemplate.delete(customerKey);

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