package org.example.adapterwebsocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoRateSubscriptionCheckResponse {

    private boolean hasActiveSubscriptions;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CryptoRateSubscriptionCheckResponse that = (CryptoRateSubscriptionCheckResponse) o;
        return hasActiveSubscriptions == that.hasActiveSubscriptions;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hasActiveSubscriptions);
    }
}
