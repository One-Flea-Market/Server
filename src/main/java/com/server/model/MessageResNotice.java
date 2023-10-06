package com.server.model;

import lombok.*;

import java.util.List;

@Data
public class MessageResNotice {
    private boolean next;
    private String message;
    private List noticeList;

    public MessageResNotice() {
        this.next = false;
        this.message = null;
        this.noticeList = null;
    }
}
