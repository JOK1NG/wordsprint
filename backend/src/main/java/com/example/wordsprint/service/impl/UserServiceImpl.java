package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.entity.User;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.security.AuthenticatedUser;
import com.example.wordsprint.service.UserService;
import com.example.wordsprint.vo.UserInfoResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, principal.getId())
                .eq(User::getStatus, 1));
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效");
        }

        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(), user.getRole());
    }
}
