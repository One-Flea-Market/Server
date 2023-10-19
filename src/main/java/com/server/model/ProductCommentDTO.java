package com.server.model;

import lombok.Data;

@Data
public class ProductCommentDTO {
    private int id;
    private String body;
    private String date;
    private int userId;
    private int productSeq;
    private boolean onself;
}
