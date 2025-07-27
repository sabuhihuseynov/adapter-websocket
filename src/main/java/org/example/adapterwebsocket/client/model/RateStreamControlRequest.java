package org.example.adapterwebsocket.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.adapterwebsocket.model.CurrencyPair;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateStreamControlRequest {

    private CurrencyPair currencyPair;
    private boolean enabled;

}
