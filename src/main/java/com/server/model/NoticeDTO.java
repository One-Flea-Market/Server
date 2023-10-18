package com.server.model;

import lombok.Data;

@Data
public class NoticeDTO {

    private int id;
    private String title;
    private String date;
    private String body;
}
