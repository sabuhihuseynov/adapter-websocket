package org.example.adapterwebsocket.model;

import java.security.Principal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Principal {

    private String userId;
    private String customerId;
    private String individualId;

    @Override
    public String getName() {
        return userId;
    }
}
