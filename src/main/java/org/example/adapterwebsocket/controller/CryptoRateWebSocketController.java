package org.example.adapterwebsocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.security.Principal;
import org.example.adapterwebsocket.model.User;
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
                                SimpMessageHeaderAccessor headerAccessor,
                                Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        String customerId = getCustomerIdFromPrincipal(principal);

        log.info("Subscribe request from session {} (customer {}) for pair {}",
                sessionId, customerId, currencyPair);

        if (customerId != null) {
            subscriptionService.subscribe(customerId, currencyPair);
        } else {
            log.error("Cannot subscribe - no customerId found in principal for session {}", sessionId);
        }
    }

    @MessageMapping("/crypto.rate.unsubscribe")
    public void unsubscribeFromPair(@Payload CurrencyPair currencyPair,
                                    SimpMessageHeaderAccessor headerAccessor,
                                    Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        String customerId = getCustomerIdFromPrincipal(principal);

        log.info("Unsubscribe request from session {} (customer {}) for pair {}",
                sessionId, customerId, currencyPair);

        if (customerId != null) {
            subscriptionService.unsubscribe(customerId, currencyPair);
        } else {
            log.error("Cannot unsubscribe - no customerId found in principal for session {}", sessionId);
        }
    }

    private String getCustomerIdFromPrincipal(Principal principal) {
        return (principal instanceof User user)
                ? user.getCustomerId()
                : null;
    }
}

