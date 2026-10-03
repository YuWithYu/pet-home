package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // ===== 以下方法由 dao.UserMapper（旧包）合并而来，XML 绑定见 resources/mapper/UserMapper.xml =====

    User selectByPrimaryKey(Long id);

    User selectByUsername(String username);

    User selectByUserEmail(String email);

    int updateStatus(Long userId, int status);

    int insertUser(User user);

    int updateHeader(Long userId, String headerUrl);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    /**
     * 统计用户关注数（该用户关注了多少人）
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Integer countFollow(Long userId);

    /**
     * 统计用户粉丝数（有多少人关注了该用户）
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Integer countFans(Long userId);

    /**
     * 统计用户动态数
     */
    @Select("SELECT COUNT(*) FROM post WHERE user_id = #{userId} AND status = 1")
    Integer countPost(Long userId);

    /**
     * 统计用户获赞数（该用户发布的所有动态的总获赞数）
     */
    @Select("SELECT COUNT(*) FROM post_like WHERE post_id IN (SELECT id FROM post WHERE user_id = #{userId} AND status = 1)")
    Integer countLikes(Long userId);

    /**
     * 检查昵称是否已被使用（排除指定用户ID）
     */
    @Select("SELECT COUNT(*) FROM user WHERE nickname = #{nickname} AND status = 1 AND (#{excludeUserId} IS NULL OR id != #{excludeUserId})")
    Integer checkNicknameExists(@Param("nickname") String nickname,
                                @Param("excludeUserId") Long excludeUserId);

    /**
     * 原子扣减积分（数据库层防并发负数：积分充足才扣，返回影响行数）
     */
    @Update("UPDATE user SET points = points - #{points} WHERE id = #{userId} AND points >= #{points}")
    int decrementPoints(@Param("userId") Long userId, @Param("points") int points);

    // ===== 以下方法为 CacheSyncTask 性能优化（#45）新增，避免循环 N+1 =====

    /**
     * 批量获取活跃用户 ID（按 id 升序，限制 limit 条）
     */
    @Select("SELECT id FROM user WHERE status = 1 ORDER BY id ASC LIMIT #{limit}")
    List<Long> selectActiveUserIds(@Param("limit") int limit);

    /**
     * 批量统计多个用户的关注数（GROUP BY 聚合，1 次查询替代 N 次）
     * @return List of {userId, cnt}
     */
    @Select({
        "<script>",
        "SELECT follower_id AS userId, COUNT(*) AS cnt FROM user_follow",
        "WHERE follower_id IN",
        "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "GROUP BY follower_id",
        "</script>"
    })
    List<Map<String, Object>> batchCountFollow(@Param("userIds") List<Long> userIds);

    /**
     * 批量统计多个用户的粉丝数
     */
    @Select({
        "<script>",
        "SELECT following_id AS userId, COUNT(*) AS cnt FROM user_follow",
        "WHERE following_id IN",
        "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "GROUP BY following_id",
        "</script>"
    })
    List<Map<String, Object>> batchCountFans(@Param("userIds") List<Long> userIds);

    /**
     * 批量统计多个用户的动态数
     */
    @Select({
        "<script>",
        "SELECT user_id AS userId, COUNT(*) AS cnt FROM post",
        "WHERE status = 1 AND user_id IN",
        "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "GROUP BY user_id",
        "</script>"
    })
    List<Map<String, Object>> batchCountPost(@Param("userIds") List<Long> userIds);

    /**
     * 批量统计多个用户的获赞数（子查询限定 post.user_id）
     */
    @Select({
        "<script>",
        "SELECT p.user_id AS userId, COUNT(pl.id) AS cnt FROM post p",
        "LEFT JOIN post_like pl ON pl.post_id = p.id",
        "WHERE p.status = 1 AND p.user_id IN",
        "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "GROUP BY p.user_id",
        "</script>"
    })
    List<Map<String, Object>> batchCountLikes(@Param("userIds") List<Long> userIds);
}

