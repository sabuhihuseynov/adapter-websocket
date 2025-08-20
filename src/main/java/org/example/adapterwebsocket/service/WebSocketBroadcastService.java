package org.example.adapterwebsocket.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.exception.EventDataMismatchingException;
import org.example.adapterwebsocket.model.BaseEvent;
import org.example.adapterwebsocket.model.CryptoRateEventDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void broadcastCryptoRate(BaseEvent<CryptoRateEventDto> cryptoRateEvent) {
        if (StringUtils.isBlank(cryptoRateEvent.getEventId())) {
            throw new EventDataMismatchingException("eventId can not be blank");
        }

        var payload = cryptoRateEvent.getPayload();
        var currencyPair = String.format("%s-%s", payload.getFromCurrency(), payload.getToCurrency());
        simpMessagingTemplate.convertAndSend("/topic/crypto/rates/" + currencyPair, payload);
    }



}
