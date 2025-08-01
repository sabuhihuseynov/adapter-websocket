package org.example.adapterwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.CryptoRateSubscriptionCheckResponse;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.example.adapterwebsocket.service.CryptoRateSubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/crypto/rate/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class CryptoRateSubscriptionRestController {

    private final CryptoRateSubscriptionService subscriptionService;

    @GetMapping("/check")
    public CryptoRateSubscriptionCheckResponse hasPairSubscription(CurrencyPair currencyPair) {
        boolean hasActiveSubscriptions = subscriptionService.hasPairSubscription(currencyPair);
        log.debug("Subscription check for {}: {}", currencyPair, hasActiveSubscriptions);
        return new CryptoRateSubscriptionCheckResponse(hasActiveSubscriptions);
    }
}
