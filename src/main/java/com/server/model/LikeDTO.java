package com.server.model;

import lombok.Data;

@Data
public class LikeDTO {
    private int userId;
    private int productSeq;
    private boolean onlike;
}
