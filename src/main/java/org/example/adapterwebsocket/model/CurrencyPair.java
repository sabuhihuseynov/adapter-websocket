package org.example.adapterwebsocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPair {

    private String fromCurrency;
    private String toCurrency;

    public static CurrencyPair fromString(String pairString) {
        if (pairString == null || pairString.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency pair string cannot be null or empty");
        }

        String[] parts = pairString.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid currency pair format: " + pairString);
        }

        return new CurrencyPair(parts[0].trim(), parts[1].trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrencyPair that = (CurrencyPair) o;
        return Objects.equals(fromCurrency, that.fromCurrency) && Objects.equals(toCurrency, that.toCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency);
    }

    @Override
    public String toString() {
        return fromCurrency + "-" + toCurrency;
    }
}

