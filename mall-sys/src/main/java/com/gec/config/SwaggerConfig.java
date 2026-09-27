package com.gec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger 接口文档
 *
 * 项目原来一个接口说明都没有，交接和演示全靠口头讲。
 * 启动后浏览器打开：
 *   http://localhost:8090/mall-sys/swagger-ui/index.html
 *
 * 注意：文档页面属于静态资源，没有 token，所以要在 AuthInterceptor 的白名单里放行，
 *      否则会被 401 挡住（见 WebConfig.AUTH_EXCLUDE）。
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket mallApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 只扫描 controller 包，避免把 Spring 自带的 error 接口也扫进来
                .apis(RequestHandlerSelectors.basePackage("com.gec.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("优选商城后台接口文档")
                .description("mall-sys 后端接口一览（SpringBoot 2.3.7 + MyBatis-Plus）")
                .version("1.0")
                .contact(new Contact("jinember", "", ""))
                .build();
    }
}
