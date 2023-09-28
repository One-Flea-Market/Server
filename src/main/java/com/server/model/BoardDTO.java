package com.server.model;

import lombok.Data;

@Data
public class BoardDTO{
    private int id;
    private String strTitle;
    private String strDate;
    private String strContent;
    private int userId;
}
