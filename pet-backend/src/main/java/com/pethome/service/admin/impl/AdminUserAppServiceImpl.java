package com.pethome.service.admin.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.User;
import com.pethome.service.admin.AdminUserAppService;
import com.pethome.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminUserAppServiceImpl implements AdminUserAppService {

    @Autowired(required = false)
    private UserService userService;

    @Override
    public Long createUser(String phone, String password, String nickname) {
        requireService();
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        try {
            userService.register(phone.trim(), password, nickname);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        User created = userService.getUserByPhone(phone.trim());
        return created != null ? created.getId() : null;
    }

    @Override
    public boolean deleteUser(Long userId) {
        requireService();
        boolean deleted = userService.deleteUser(userId);
        if (deleted) {
            userService.clearUserCache(userId);
        }
        return deleted;
    }

    private void requireService() {
        if (userService == null) {
            throw new BusinessException("用户服务不可用");
        }
    }
}
