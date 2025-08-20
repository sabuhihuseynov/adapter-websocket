package org.example.adapterwebsocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoRateEventDto {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDateTime date;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CryptoRateEventDto that = (CryptoRateEventDto) o;
        return Objects.equals(fromCurrency, that.fromCurrency)
                && Objects.equals(toCurrency, that.toCurrency)
                && Objects.equals(rate, that.rate)
                && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromCurrency, toCurrency, rate, date);
    }
}

