package com.example.wordsprint.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String role;
}
