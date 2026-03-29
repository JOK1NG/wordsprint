package com.example.wordsprint.security;

import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getCurrentUserId() {
        return requireAuthenticatedUser().getId();
    }

    public String getCurrentUserRole() {
        return requireAuthenticatedUser().getRole();
    }

    public void requireAdmin() {
        if (!"ADMIN".equalsIgnoreCase(getCurrentUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可执行该操作");
        }
    }

    private AuthenticatedUser requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或 token 无效");
    }
}
