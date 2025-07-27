package org.example.adapterwebsocket.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPair {

    private String from;
    private String to;

    public static CurrencyPair fromString(String pairString) {
        if (pairString == null || !pairString.contains("-")) {
            throw new IllegalArgumentException("Invalid currency pair format: " + pairString);
        }

        String[] parts = pairString.split("-", 2);
        return new CurrencyPair(parts[0], parts[1]);
    }

    public String getTopicName() {
        return "/topic/rates/" + toString();
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
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return from + "-" + to;
    }
}

