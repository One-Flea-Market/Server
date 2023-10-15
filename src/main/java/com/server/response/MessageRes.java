package com.server.response;

import com.server.model.UserDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageRes {
    private boolean result;
    private String message;
    private Object auth;
    private UserDTO userList;

    public MessageRes() {
        this.result = false;
        this.message = null;
        this.auth = null;
        this.userList = null;
    }
}