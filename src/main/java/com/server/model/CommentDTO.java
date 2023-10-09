package com.server.model;

import lombok.Data;

@Data
public class CommentDTO {
    private int commentSeq;
    private String commentBody;
    private String commentDate;
    private int userId;
    private int boardSeq;
}
