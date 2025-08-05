package org.example.adapterwebsocket.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.adapterwebsocket.dao.repository.UnDisabledCryptoStreamingRepository;
import org.example.adapterwebsocket.service.CryptoRateSubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoStreamingRetryScheduler {

    private final CryptoRateSubscriptionService service;
    private final UnDisabledCryptoStreamingRepository unDisabledCryptoStreamingRepository;

    @Scheduled(cron = "${application.crypto-rate-streaming-disable-scheduler-cron}")
    @SchedulerLock(name = "disableRateStreamingScheduler")
    public void startCryptoRatesScheduler() {
        log.info("Started Disable Crypto Rate Streaming Scheduler at: {}", LocalDateTime.now());
        var unDisabledCryptoStreamingEntities = unDisabledCryptoStreamingRepository.findAll();
        service.processFailedDisables(unDisabledCryptoStreamingEntities);
        log.info("Ended Disable Crypto Rate Streaming Scheduler at: {}", LocalDateTime.now());
    }

}
