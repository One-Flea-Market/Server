package com.server.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductRequestDTO {
    private String strProductTitle;
    private String strProductStatus;
    private String strProductContent;
    private String strProductDate;
    private String strProductPrice;
    private List<String> strProductLink;
    private List<MultipartFile> imageFiles;
}