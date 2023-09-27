package com.server.model;

import lombok.Data;

@Data
public class UserDTO {
    private int id;
    private String strUserName;
    private String strEmail;
    private String strPassword;
    private String strRole;
    private String strPhoneNumber;
}
