package org.example.adapterwebsocket.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.adapterwebsocket.client.AuthClient;
import org.example.adapterwebsocket.client.model.UserTokenPayload;
import org.example.adapterwebsocket.model.User;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final AuthClient authClient;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("Connection request to WebSocket...");
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null) {
                return message;
            }
            // UserTokenPayload userTokenPayload = authClient.verify(authHeader);
            UserTokenPayload userTokenPayload = getUserTokenPayload(authHeader);

            accessor.setUser(new User(userTokenPayload.getUserId(), userTokenPayload.getCustomerId(), Instant.now()));

            log.info("User {} authenticated for WebSocket session {}", userTokenPayload.getUserId(),
                    accessor.getSessionId());
        }

        return message;
    }

    public UserTokenPayload getUserTokenPayload(String authHeader) {
        String auth = authHeader.replace("Bearer ", "");
        String[] tokens = auth.split("-");
        return UserTokenPayload.builder()
                .customerId(tokens[0])
                .userId(tokens[1])
                .build();
    }
}
