package com.jwt.utils;

import lombok.Data;

@Data
public class AuthRequest {

    private String username;
    private String password;
}