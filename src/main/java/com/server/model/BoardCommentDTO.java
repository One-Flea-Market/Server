package com.server.model;

import lombok.Data;

@Data
public class BoardCommentDTO {
    private int id;
    private String body;
    private String date;
    private int userId;
    private int boardSeq;
    private boolean onself;
}
