package com.server.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductRequestDTO {
    private String title;
    private String status;
    private String body;
    private String date;
    private String price;
    private List<String> list;
    private List<MultipartFile> imageFiles;
}