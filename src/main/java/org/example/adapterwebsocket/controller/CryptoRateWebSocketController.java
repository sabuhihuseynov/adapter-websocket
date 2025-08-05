package org.example.adapterwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.service.CryptoRateSubscriptionService;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CryptoRateWebSocketController {

    private final CryptoRateSubscriptionService subscriptionService;

    @MessageMapping("/crypto.rate.subscribe")
    public void subscribeToPair(@Payload CurrencyPair currencyPair,
                                SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        subscriptionService.subscribe(sessionId, currencyPair);
    }

    @MessageMapping("/crypto.rate.unsubscribe")
    public void unsubscribeFromPair(@Payload CurrencyPair currencyPair,
                                    SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        subscriptionService.unsubscribe(sessionId, currencyPair);
    }
}

