package com.server.model;

import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ProductDTO {
    private int id;
    private String title;
    private String status;
    private String body;
    private String date;
    private String price;
    private String list;
    private int userId;
    private boolean onlike;
    private boolean onself;
}
