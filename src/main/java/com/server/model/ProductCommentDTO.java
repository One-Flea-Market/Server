package com.server.model;

import lombok.Data;

@Data
public class ProductCommentDTO {
    private int productCommentSeq;
    private String productCommentBody;
    private String productCommentDate;
    private int userId;
    private int productSeq;
    private boolean onself;
}
