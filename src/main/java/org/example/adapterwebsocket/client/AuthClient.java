package org.example.adapterwebsocket.client;

import jakarta.validation.constraints.NotBlank;
import org.example.adapterwebsocket.client.model.UserTokenPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-auth", url = "localhost:8081/v1/internal/auth")
public interface AuthClient {

    @GetMapping("/verify")
    UserTokenPayload verify(@NotBlank @RequestHeader("Authorization") String authorization);

}
