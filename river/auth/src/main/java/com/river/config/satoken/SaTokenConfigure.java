package com.river.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义详细的拦截路由
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/pay/**",
                        "/api/juhe/**",
                        "/api/social/**",
                        "/sys/siteConfig/public",   // 网站配置公开接口
                        "/sys/siteConfig/key/**",   // 网站配置按key获取
                        "/swagger-ui/**",          // knife4j接口文档
                        "/webjars/**",        // knife4j相关资源
                        "/v3/api-docs/**",     // openapi接口文档
                        "/doc.html",     // openapi接口文档
                        "/favicon.ico",     // openapi接口文档
                        "/swagger-resources",
                        "/localFile/**",
                        "/ws/**"              // WebSocket连接
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有请求路径生效
                // 允许你的前端来源（精准授权，解决跨域问题，替换为你的实际前端地址，不要用*）
                .allowedOrigins("http://117.72.179.87:86","http://117.72.179.87:87","*")
                // 允许的HTTP请求方法（GET/POST等，覆盖常见场景）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许携带自定义请求头（如Token、Content-Type等）
                .allowedHeaders("*")
                // 允许携带Cookie、Session等认证信息（如需登录状态保持，必须开启）
                .allowCredentials(true)
                // 预检请求（OPTIONS）的缓存时间，3600秒内无需重复发送预检请求，提升性能
                .maxAge(3600);
    }
}
