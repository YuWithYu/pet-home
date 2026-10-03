package com.pethome.config;

import com.pethome.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${upload.path:./upload/}")
    private String uploadPath;
    
    @Autowired
    private RoleInterceptor roleInterceptor;

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();

        // 注册 Java 8 日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置 LocalDateTime 的序列化和反序列化格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        // 入参兼容多格式：yyyy-MM-dd HH:mm:ss（本项目惯例）与 yyyy-MM-dd'T'HH:mm:ss（ISO，Element Plus 日期控件默认）
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        // 配置 LocalDate 为 yyyy-MM-dd 字符串，避免序列化为数组导致前端日期错位（如月份差 1）
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 允许忽略未知字段，避免反序列化错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(stringHttpMessageConverter());
        converters.add(mappingJackson2HttpMessageConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/api/admin/login",
                    "/api/user/login",
                    "/api/user/register",
                    "/api/user/forgot-password",  // 忘记密码接口不需要认证
                    "/api/user/wxapp/login",  // 微信小程序登录接口不需要认证
                    "/api/sms/**",  // 短信验证码接口不需要认证
                    "/api/complaint/submit",  // 投诉举报提交可不登录
                    "/public/**",
                    "/static/**",
                    "/upload/**",
                    "/error",
                    "/favicon.ico",
                    // springdoc API 文档路径（文档本身由 SWAGGER_ENABLED 开关控制，生产环境关闭）
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，允许访问上传的文件
        // 使用配置文件中的路径
        log.info("上传文件目录: {}", uploadPath);

        // 规范化为绝对路径：transferTo()/mkdirs() 均按 JVM 工作目录解析相对路径，
        // 而 file: 资源位置默认也按工作目录解析；统一转绝对路径确保映射与实际写入目录一致
        String normalizedPath = new java.io.File(uploadPath).getAbsoluteFile().getAbsolutePath().replace("\\", "/");
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        // 映射 /upload/** 到本地目录
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + normalizedPath)
                .setCachePeriod(3600); // 缓存1小时

        // 配置前端静态资源映射（开发环境使用，仅用于Vue构建文件和默认资源）
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 静态资源缓存24小时
    }

    /**
     * 兼容多格式的 LocalDateTime 反序列化器
     *
     * <p>出参统一为 yyyy-MM-dd HH:mm:ss；入参同时兼容：
     * <ul>
     *   <li>yyyy-MM-dd HH:mm:ss（空格分隔，本项目/小程序端惯例）</li>
     *   <li>yyyy-MM-dd'T'HH:mm:ss（ISO-8601，Element Plus 日期控件默认提交格式）</li>
     *   <li>yyyy-MM-dd（仅日期，按当日零点处理）</li>
     * </ul>
     */
    private static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final DateTimeFormatter STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws java.io.IOException {
            String text = parser.getText();
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            String value = text.trim();
            // ISO 分隔符 T 归一化为空格
            if (value.length() > 10 && value.charAt(10) == 'T') {
                value = value.substring(0, 10) + ' ' + value.substring(11);
            }
            // 截断毫秒部分（如 2026-08-22 00:00:00.123）
            int dotIndex = value.indexOf('.', 19);
            if (dotIndex > 0) {
                value = value.substring(0, dotIndex);
            }
            if (value.length() == 10) {
                return LocalDate.parse(value).atStartOfDay();
            }
            return LocalDateTime.parse(value, STANDARD);
        }
    }
}