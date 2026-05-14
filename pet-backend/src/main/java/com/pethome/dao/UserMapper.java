package com.pethome.dao;

import com.pethome.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
//    int deleteByPrimaryKey(Long id);
//
//    int insert(User record);
//
//    int insertSelective(User record);
//
//
//    int updateByPrimaryKeySelective(User record);
//
//    int updateByPrimaryKey(User record);

    User selectByPrimaryKey(Long id);

    User selectByUsername(String username);

    User selectByUserEmail(String email);

    int updateStatus(Long userId, int status); // 更新用户激活状态

    int insertUser(User user);

    int updateHeader(Long userId, String headerUrl);

}