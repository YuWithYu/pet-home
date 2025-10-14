package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.User;
import com.pethome.mapper.UserMapper;
import com.pethome.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (user.getPhone() != null) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", user.getPhone());
            existingUser = userMapper.selectOne(queryWrapper);
            if (existingUser != null) {
                throw new RuntimeException("手机号已存在");
            }
        }

        // 密码加密
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }

        // 设置默认值
        user.setStatus(1);
        user.setRole("user");
        user.setMemberLevel(1);
        user.setPoints(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        if (!user.getStatus().equals(1)) {
            throw new RuntimeException("账号已被禁用");
        }

        // 使用bcrypt验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token（这里简化返回用户名作为token）
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    @Override
    public String loginByPhone(String phone, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new RuntimeException("手机号不存在");
        }

        if (!user.getStatus().equals(1)) {
            throw new RuntimeException("账号已被禁用");
        }

        // 使用bcrypt验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getUserByPhone(String phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public IPage<User> getUserList(Page<User> page) {
        return userMapper.selectPage(page, null);
    }

    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new RuntimeException("旧密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        updateUser.setUpdateTime(LocalDateTime.now());

        return userMapper.updateById(updateUser) > 0;
    }
}
