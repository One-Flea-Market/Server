package com.server.model;

import lombok.Data;

import java.util.List;

@Data
public class MessageResBoard {
    private boolean oneself;
    private boolean next;
    private String message;
    private List boardList;

    public MessageResBoard() {
        this.next = false;
        this.oneself = false;
        this.message = null;
        this.boardList = null;
    }
}
