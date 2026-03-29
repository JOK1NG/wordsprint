package com.example.wordsprint.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String tokenType;

    private Long expireIn;

    private UserInfoResponse userInfo;
}
