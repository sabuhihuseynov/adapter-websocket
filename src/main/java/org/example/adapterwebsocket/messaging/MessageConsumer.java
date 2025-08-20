package org.example.adapterwebsocket.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.BaseEvent;
import org.example.adapterwebsocket.model.CryptoRateEventDto;
import org.example.adapterwebsocket.service.WebSocketBroadcastService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.example.adapterwebsocket.model.constant.RabbitConstants.Q_CRYPTO_RATE_LIVE;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {

    private final WebSocketBroadcastService webSocketBroadcastService;

    @RabbitListener(queues = Q_CRYPTO_RATE_LIVE)
    public void createNotificationListener(BaseEvent<CryptoRateEventDto> cryptoRateEvent) {
        log.info("Start crypto rate event: {}", cryptoRateEvent);
        webSocketBroadcastService.broadcastCryptoRate(cryptoRateEvent);
        log.info("End crypto rate event: {}", cryptoRateEvent);
    }

}
