package com.pethome.service;

import com.pethome.entity.LoginTicket;
import com.pethome.entity.Message;
import com.pethome.entity.User;
import com.pethome.vo.UserVo;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * @author linyuhong
 * @date 2019/9/1
 */
public interface IUserService {

    Map<String, Object> login(String username, String password);

    LoginTicket findLoginTicket(String key);

    UserVo findUserById(Long id);

    void logout(String ticket);

    Map<String, Object> register(User user);

    int activation(Long userId, String code);

    UserVo findUserByName(String username);

    int updateHeader(Long userId, String url);

    Collection<? extends GrantedAuthority> getAuthorities(Long userId);


}
