package com.pethome.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一 JSON 工具类（Jackson 实现）
 *
 * 背景：fastjson 1.2.76 存在已知反序列化 RCE 漏洞（CVE-2022-25845），
 * 全项目统一迁移至 Jackson，杜绝 autoType 绕过风险。
 *
 * 说明：日期格式固定为 yyyy-MM-dd HH:mm:ss，与旧 fastjson 缓存数据及
 * WebConfig 中的全局 Jackson 配置保持一致，保证 Redis 中历史缓存可继续解析。
 */
public class JSONUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private JSONUtil() {
    }

    /** 暴露内部 Mapper，供复杂场景（convertValue 等）复用同一套配置 */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /** 序列化为 JSON 字符串 */
    public static String toJSONString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    /** 反序列化为指定类型对象 */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    /** 反序列化为指定元素的列表 */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + e.getMessage(), e);
        }
    }

    /** 解析为 JsonNode 树，用于结构不确定的动态 JSON 处理 */
    public static JsonNode parseTree(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 解析失败: " + e.getMessage(), e);
        }
    }

    /** 安全取文本字段：字段缺失、null 或非文本类型时返回 null */
    public static String getText(JsonNode node, String field) {
        JsonNode v = node == null ? null : node.get(field);
        return (v != null && v.isTextual()) ? v.asText() : null;
    }

    /** 兼容旧调用：构建 {code, msg, ...map} 结构的响应 JSON */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", code);
        data.put("msg", msg);
        if (map != null) {
            data.putAll(map);
        }
        return toJSONString(data);
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, "", null);
    }

}
