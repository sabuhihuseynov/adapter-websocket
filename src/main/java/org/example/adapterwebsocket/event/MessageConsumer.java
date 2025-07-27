package org.example.adapterwebsocket.event;

import static org.example.adapterwebsocket.model.constant.RabbitConstants.Q_CRYPTO_RATE_NOTIFICATION;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.model.BaseEvent;
import org.example.adapterwebsocket.model.CryptoRateEventDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @RabbitListener(queues = Q_CRYPTO_RATE_NOTIFICATION)
    public void createNotificationListener(BaseEvent<CryptoRateEventDto> cryptoRateEvent) {
        log.info("Start crypto rate event: {}", cryptoRateEvent);
        var payload = cryptoRateEvent.getPayload();
        var currencyPair = String.format("%s-%s", payload.getFrom(), payload.getTo());
        simpMessagingTemplate.convertAndSend("/topic/crypto/rates/" + currencyPair, payload);
        log.info("End crypto rate event: {}", cryptoRateEvent);
    }

}
