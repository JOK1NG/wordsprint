package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.dto.LoginRequest;
import com.example.wordsprint.dto.RegisterRequest;
import com.example.wordsprint.entity.User;
import com.example.wordsprint.entity.UserPoints;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.mapper.UserPointsMapper;
import com.example.wordsprint.security.JwtTokenProvider;
import com.example.wordsprint.service.AuthService;
import com.example.wordsprint.vo.LoginResponse;
import com.example.wordsprint.vo.RegisterResponse;
import com.example.wordsprint.vo.UserInfoResponse;
import com.example.wordsprint.service.RedisRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserPointsMapper userPointsMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRankService redisRankService;

    public AuthServiceImpl(UserMapper userMapper,
                           UserPointsMapper userPointsMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           RedisRankService redisRankService) {
        this.userMapper = userMapper;
        this.userPointsMapper = userPointsMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisRankService = redisRankService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();

        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(user.getId());
        userPoints.setTotalPoints(0);
        userPoints.setStreakDays(0);
        userPoints.setMaxStreakDays(0);
        userPoints.setTotalStudied(0);
        userPoints.setTotalCorrect(0);
        userPoints.setTotalDurationSeconds(0);
        userPointsMapper.insert(userPoints);

        redisRankService.updateUserPoints(user.getId(), 0);
        redisRankService.updateUserStreak(user.getId(), 0);

        log.info("用户注册成功: userId={}, username={}", user.getId(), username);
        return new RegisterResponse(user.getId(), user.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前用户已被禁用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtTokenProvider.generateToken(user.getId());
        log.info("用户登录成功: userId={}, username={}", user.getId(), username);
        UserInfoResponse userInfo = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole()
        );
        return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpireSeconds(), userInfo);
    }
}
