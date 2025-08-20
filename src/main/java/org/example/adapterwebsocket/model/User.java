package org.example.adapterwebsocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.time.Instant;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Principal {

    private String userId;
    private String customerId;
    private Instant tokenExpiration;

    public boolean isTokenExpired() {
        return tokenExpiration == null || tokenExpiration.isBefore(Instant.now());
    }

    @Override
    public String getName() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(userId, user.userId)
                && Objects.equals(customerId, user.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, customerId);
    }
}
