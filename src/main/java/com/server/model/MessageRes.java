package com.server.model;

import lombok.*;

@Data
public class MessageRes {
    private boolean result;
    private String message;
    private Object auth;

    public MessageRes() {
        this.result = false;
        this.message = null;
        this.auth = null;
    }
}