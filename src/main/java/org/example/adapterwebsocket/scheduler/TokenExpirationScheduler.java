package org.example.adapterwebsocket.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.adapterwebsocket.service.SessionManagerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenExpirationScheduler {

    private final SessionManagerService sessionManagerService;

    @Scheduled(cron = "0 */3 * * * ?")
    @SchedulerLock(name = "validateActiveSessions", lockAtMostFor = "4m")
    public void startTokenExpirationScheduler() {
        log.info("Started Token Expiration Check Scheduler at: {}", LocalDateTime.now());
        sessionManagerService.checkTokenExpirations();
        log.info("Ended Token Expiration Check Scheduler at: {}", LocalDateTime.now());
    }

}
