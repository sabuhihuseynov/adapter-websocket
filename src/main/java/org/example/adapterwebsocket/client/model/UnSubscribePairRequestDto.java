package org.example.adapterwebsocket.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.adapterwebsocket.model.CurrencyPair;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnSubscribePairRequestDto {

    private Set<CurrencyPair> currencyPairs;

}
