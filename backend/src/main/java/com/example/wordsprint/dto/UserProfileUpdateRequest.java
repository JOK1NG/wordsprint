package com.example.wordsprint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    @Size(max = 255)
    private String avatar;
}
