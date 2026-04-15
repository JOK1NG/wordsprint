package com.example.wordsprint.controller;

import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.common.Result;
import com.example.wordsprint.dto.LoginRequest;
import com.example.wordsprint.dto.RegisterRequest;
import com.example.wordsprint.security.JwtBlacklistService;
import com.example.wordsprint.security.LoginRateLimiter;
import com.example.wordsprint.service.AuthService;
import com.example.wordsprint.vo.LoginResponse;
import com.example.wordsprint.vo.RegisterResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtBlacklistService jwtBlacklistService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthService authService,
                          JwtBlacklistService jwtBlacklistService,
                          LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.jwtBlacklistService = jwtBlacklistService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        if (!loginRateLimiter.isAllowed(clientIp)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "登录请求过于频繁，请稍后再试");
        }
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());
            jwtBlacklistService.blacklist(token);
        }
        return Result.success();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
