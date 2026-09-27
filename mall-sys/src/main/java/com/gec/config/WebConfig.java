package com.gec.config;

import com.gec.components.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 鉴权白名单：
     *  - /User/login            登录接口本身
     *  - /shop/**               C 端商城（游客可浏览，会员态由前端自己维护）
     *  - Brand / Sku / PublishGoods 的 showImg  图片回显，img 标签的请求带不了自定义头
     *  - /error                 SpringBoot 默认错误转发
     */
    private static final String[] AUTH_EXCLUDE = {
            "/User/login",
            "/shop/**",
            "/Brand/showImg/**",
            "/Sku/showImg/**",
            "/PublishGoods/showImg/**",
            // Swagger 文档页面是静态资源，请求带不了 token
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**",
            "/error"
    };

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE);
    }
}
