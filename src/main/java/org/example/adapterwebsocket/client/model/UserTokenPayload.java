package org.example.adapterwebsocket.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenPayload {

    private String userId;
    private String customerId;
    private Instant expiration;

}
