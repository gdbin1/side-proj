package com.gbk.sideproj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("SideProj API Documentation")
                    .description("경기도 공매 서비스 API 문서 (Admin 전용)")
                    .version("1.0.0")
                );
    }
//    http://localhost:8080/swagger-ui.html 스웨거주소 스웩
}
