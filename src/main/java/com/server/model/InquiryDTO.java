package com.server.model;

import lombok.Data;

@Data
public class InquiryDTO {
    private int inquirySeq;
    private String inquiryTitle;
    private String inquiryBody;
    private String inquiryDate;
    private String inquiryEmail;
    private int userId;
}
