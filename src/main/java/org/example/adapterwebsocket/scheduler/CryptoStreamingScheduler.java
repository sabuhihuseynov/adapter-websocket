package org.example.adapterwebsocket.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.adapterwebsocket.dao.repository.UnDisabledCryptoPairRepository;
import org.example.adapterwebsocket.service.CryptoRateSubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoStreamingScheduler {

    private final CryptoRateSubscriptionService service;
    private final UnDisabledCryptoPairRepository unDisabledCryptoPairRepository;

    @Scheduled(cron = "0 */3 * * * ?")
    @SchedulerLock(name = "disableRateStreamingScheduler")
    public void startCryptoRatesScheduler() {
        log.info("Started Disable Crypto Rate Streaming Scheduler at: {}", LocalDateTime.now());
        var unDisabledCryptoPairEntities = unDisabledCryptoPairRepository.findAll();
        service.processFailedDisables(unDisabledCryptoPairEntities);
        log.info("Ended Disable Crypto Rate Streaming Scheduler at: {}", LocalDateTime.now());
    }

}
