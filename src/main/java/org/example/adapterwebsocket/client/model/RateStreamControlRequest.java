package org.example.adapterwebsocket.client.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.adapterwebsocket.model.CurrencyPair;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateStreamControlRequest {

    private Set<CurrencyPair> currencyPairs;
    private boolean enabled;

}
