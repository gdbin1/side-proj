package com.gbk.sideproj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // ✅ POST, GET 모두 CSRF 검사 비활성화
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/**",   // 내 API 전체 열기
                    "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**" // Swagger 허용
                ).permitAll()
                .anyRequest().permitAll() // 나머지도 일단 다 허용
            )
            .formLogin(login -> login.disable()) // 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable()); // 기본 인증창 비활성화

        return http.build();
    }
}
