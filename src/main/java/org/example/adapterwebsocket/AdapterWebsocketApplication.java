package org.example.adapterwebsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AdapterWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdapterWebsocketApplication.class, args);
    }

}
