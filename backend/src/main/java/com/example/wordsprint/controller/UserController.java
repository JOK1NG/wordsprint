package com.example.wordsprint.controller;

import com.example.wordsprint.common.Result;
import com.example.wordsprint.service.UserService;
import com.example.wordsprint.vo.UserInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me() {
        return Result.success(userService.getCurrentUser());
    }
}
