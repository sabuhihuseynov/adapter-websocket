package org.example.adapterwebsocket.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTokenPayload {

    private String issuedAt;
    private String expiration;
    private String userId;
    private String userStatus;
    private String mobileNumber;
    private String email;
    private String customerId;
    private String deviceType;
    private String deviceId;
    private String oneSignalId;
    private String loginType;
    private String role;
    private String individualId;
    private String countryOfResidence;

}
