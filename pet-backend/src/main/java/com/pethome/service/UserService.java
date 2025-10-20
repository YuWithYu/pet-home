package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.User;

public interface UserService {
    User register(User user);
    String register(String phone, String password, String nickname);
    String login(String username, String password);
    String loginByPhone(String phone, String password);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User getUserByPhone(String phone);
    IPage<User> getUserList(Page<User> page);
    boolean updateUser(User user);
    boolean deleteUser(Long id);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
