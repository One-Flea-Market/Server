package com.server.model;

import lombok.*;

import java.util.List;

@Data
public class MessageResF {
    private boolean next;
    private String message;
    private List noticeList;

    public MessageResF() {
        this.next = false;
        this.message = null;
        this.noticeList = null;
    }
}
