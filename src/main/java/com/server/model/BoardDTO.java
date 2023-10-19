package com.server.model;

import lombok.Data;

@Data
public class BoardDTO{
    private int id;
    private String title;
    private String date;
    private String body;
    private int userId;
}
