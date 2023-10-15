package com.server.model;

import lombok.Data;

@Data
public class BoardCommentDTO {
    private int boardCommentSeq;
    private String boardCommentBody;
    private String boardCommentDate;
    private int userId;
    private int boardSeq;
    private boolean onself;
}
