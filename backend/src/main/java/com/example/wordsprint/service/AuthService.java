package com.example.wordsprint.service;

import com.example.wordsprint.dto.LoginRequest;
import com.example.wordsprint.dto.RegisterRequest;
import com.example.wordsprint.vo.LoginResponse;
import com.example.wordsprint.vo.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
