package org.example.adapterwebsocket.client;

import org.example.adapterwebsocket.client.model.UnSubscribePairRequestDto;
import org.example.adapterwebsocket.model.CurrencyPair;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "crypto-currency", url = "localhost:8082/v1/crypto-currencies")
public interface CryptoCurrencyClient {

    @PostMapping("/rates/subscribe")
    void enableRateStreaming(@RequestBody CurrencyPair currencyPair);

    @PostMapping("/rates/unsubscribe")
    void disableRateStreaming(@RequestBody UnSubscribePairRequestDto requestDto);

}
