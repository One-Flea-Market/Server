package com.server.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BoardRequestDTO {
    private String title;
    private String date;
    private String body;
}