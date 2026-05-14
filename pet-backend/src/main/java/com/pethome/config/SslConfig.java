package com.pethome.config;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * SSL/HTTPS配置类
 * 配置Spring Boot同时支持HTTP和HTTPS
 * 
 * @author PetHome
 */
@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true", matchIfMissing = false)
public class SslConfig {

    private static final Logger logger = LoggerFactory.getLogger(SslConfig.class);

    @Value("${server.http.port:8080}")
    private int httpPort;  // HTTP端口，独立配置，避免与server.port冲突

    @Value("${server.ssl.port:8443}")
    private int httpsPort;

    @Value("${server.ssl.key-store:classpath:keystore.p12}")
    private String keyStore;

    @Value("${server.ssl.key-store-password:changeit}")
    private String keyStorePassword;

    @Value("${server.ssl.key-store-type:PKCS12}")
    private String keyStoreType;

    @PostConstruct
    public void init() {
        logger.info("==========================================");
        logger.info("🔒 SSL配置已启用 - 同时支持HTTP和HTTPS");
        logger.info("📍 HTTP端口: {}", httpPort);
        logger.info("🔒 HTTPS端口: {}", httpsPort);
        logger.info("==========================================");
    }

    /**
     * 配置Tomcat同时支持HTTP和HTTPS
     * 开发环境：允许HTTP和HTTPS同时访问
     * 生产环境：可以启用SecurityConstraint强制HTTPS
     * 
     * 注意：当server.ssl.enabled=true时，Spring Boot会在server.port上启动HTTPS
     * 当server.ssl.enabled=false时，Spring Boot会在server.port上启动HTTP
     * 
     * 如果同时需要HTTP和HTTPS，需要：
     * 1. server.port = 8443 (HTTPS端口)
     * 2. server.ssl.enabled = true
     * 3. HTTP连接器使用8080端口
     * 
     * 此配置类只在 server.ssl.enabled=true 时才会生效
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            // 注释掉SecurityConstraint，允许HTTP和HTTPS同时访问（开发环境需要）
            // 生产环境可以取消注释，强制所有请求使用HTTPS
            /*
            @Override
            protected void postProcessContext(org.apache.catalina.Context context) {
                // 配置安全约束（强制HTTPS）
                org.apache.catalina.deploy.SecurityConstraint securityConstraint = new org.apache.catalina.deploy.SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                org.apache.catalina.deploy.SecurityCollection collection = new org.apache.catalina.deploy.SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
            */
        };
        
        // 当SSL启用时，添加HTTP连接器（用于开发环境）
        // 配置：HTTPS在server.port(8443)上，HTTP在httpPort(8080)上
        // 这样API请求可以用HTTP，图片加载用HTTPS（小程序要求）
        tomcat.addAdditionalTomcatConnectors(createHttpConnector());
        
        return tomcat;
    }

    /**
     * 创建HTTP连接器
     * 注意：当server.ssl.enabled=true时，Spring Boot会在server.port上启动HTTPS
     * 所以配置应该是：
     * - server.port = 8443 (HTTPS端口)
     * - HTTP连接器使用 8080 (httpPort)
     */
    private Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        // HTTP使用8080端口（httpPort）
        // HTTPS使用8443端口（通过server.port配置）
        connector.setPort(httpPort);  // 8080
        connector.setSecure(false);
        // 不重定向，允许HTTP和HTTPS同时访问（开发环境需要）
        // 生产环境可以启用重定向：connector.setRedirectPort(httpsPort);
        
        logger.info("✅ HTTP连接器已创建，端口: {}", httpPort);
        return connector;
    }
}
