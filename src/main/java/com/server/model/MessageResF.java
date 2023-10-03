package com.server.model;

import lombok.*;

@Data
public class MessageResF {
    private boolean result;
    private String message;
    private Object auth;

    public MessageResF() {
        this.result = false;
        this.message = null;
        this.auth = null;
    }
}
