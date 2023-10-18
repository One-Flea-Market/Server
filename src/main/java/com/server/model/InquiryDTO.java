package com.server.model;

import lombok.Data;

@Data
public class InquiryDTO {
    private int id;
    private String title;
    private String body;
    private String date;
    private String email;
    private int userId;
}
