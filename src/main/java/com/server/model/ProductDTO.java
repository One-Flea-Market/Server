package com.server.model;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private boolean onlike;
    private boolean onself;
    private Set<UserDTO> usersWhoLiked = new HashSet<>();
}
