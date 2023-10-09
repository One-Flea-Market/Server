package com.server.response;

import com.server.model.BoardDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageResBoard {
    private boolean oneself;
    private boolean result;
    private boolean next;
    private String message;
    private List<BoardDTO> boardList;

    public MessageResBoard() {
        this.next = false;
        this.result = false;
        this.oneself = false;
        this.message = null;
        this.boardList = new ArrayList<>();
    }
}
