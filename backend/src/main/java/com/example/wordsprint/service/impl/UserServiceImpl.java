package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.dto.UserProfileUpdateRequest;
import com.example.wordsprint.entity.User;
import com.example.wordsprint.mapper.UserMapper;
import com.example.wordsprint.security.CurrentUserProvider;
import com.example.wordsprint.service.UserService;
import com.example.wordsprint.vo.UserInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final CurrentUserProvider currentUserProvider;

    public UserServiceImpl(UserMapper userMapper, CurrentUserProvider currentUserProvider) {
        this.userMapper = userMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        return toUserInfoResponse(getActiveUserById(currentUserProvider.getCurrentUserId()));
    }

    @Override
    @Transactional
    public UserInfoResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getActiveUserById(userId);

        String nickname = request.getNickname().trim();
        if (nickname.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "昵称不能为空");
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "昵称长度需为 2-20 个字符");
        }

        String avatar = request.getAvatar();
        if (avatar != null) {
            avatar = avatar.trim();
            if (avatar.isEmpty()) {
                avatar = null;
            }
        }

        user.setNickname(nickname);
        user.setAvatar(avatar);
        userMapper.updateById(user);

        return toUserInfoResponse(user);
    }

    private User getActiveUserById(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getStatus, 1));
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效");
        }
        return user;
    }

    private UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getAvatar(), user.getRole());
    }
}
