package org.example.adapterwebsocket.client;

import org.example.adapterwebsocket.client.model.RateStreamControlRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "crypto-currency", url = "localhost:8099/v1/crypto-currencies")
public interface CryptoCurrencyClient {

    @PostMapping("/rate/streaming")
    void controlRateStreaming(RateStreamControlRequest request);

}
