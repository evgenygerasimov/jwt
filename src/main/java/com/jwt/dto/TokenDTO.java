package com.jwt.dto;

import lombok.Data;

@Data
public class TokenDTO {

    private String accessToken;
    private String refreshToken;
}