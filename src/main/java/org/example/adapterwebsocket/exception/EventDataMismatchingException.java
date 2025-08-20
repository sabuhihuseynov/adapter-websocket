package org.example.adapterwebsocket.exception;

import lombok.Getter;

import static org.example.adapterwebsocket.exception.constant.ErrorCode.EVENT_DATA_MISMATCHING;

@Getter
public class EventDataMismatchingException extends RuntimeException {

    private final String errorCode;
    private final String message;

    public EventDataMismatchingException(String message) {
        this.message = message;
        this.errorCode = EVENT_DATA_MISMATCHING;
    }

}
