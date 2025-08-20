package org.example.adapterwebsocket.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class WebSocketConnectionEstablishedEvent extends ApplicationEvent {

    private final transient WebSocketSession webSocketSession;

    public WebSocketConnectionEstablishedEvent(Object source, WebSocketSession webSocketSession) {
        super(source);
        this.webSocketSession = webSocketSession;
    }
}
