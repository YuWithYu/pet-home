package com.pethome.util;

import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author linyuhong
 * @date 2019/9/1
 */
public class MD5Encoder {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    public static String md5(String key) {
        if (isBlank(key)) {  // " " or   ---> true
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    private static boolean isBlank(String str) { return str == null || str.trim().isEmpty(); }
}
