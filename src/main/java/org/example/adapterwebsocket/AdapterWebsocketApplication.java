package org.example.adapterwebsocket;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "3m", defaultLockAtLeastFor = "1m")
public class AdapterWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdapterWebsocketApplication.class, args);
    }

}
