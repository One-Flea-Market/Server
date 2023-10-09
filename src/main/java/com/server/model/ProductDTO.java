package com.server.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private int productSeq;
    private String strProductTitle;
    private String strProductStatus;
    private String strProductContent;
    private String strProductDate;
    private String strProductPrice;
    private String strProductLink;
    private int userId;
    private int onLike;
}
