package com.server.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserDTO {
    private int id;
    private String username;
    private String email;
    private String passWord;
    private String role;
    private String phone_number;
}
