package com.gbk.sideproj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ✅ ApiConfig
 * - 공공데이터 ServiceKey는 환경변수에서 불러오고
 * - API 기본 URL은 properties에서 유지
 */
@Configuration
public class ApiConfig {

    // ✅ 환경변수에서 불러오기
    @Value("${api.serviceKey}")
    private String serviceKey;

    // ✅ 기존처럼 application.properties에서 읽기
    @Value("${api.url}")
    private String baseUrl;

    public String getServiceKey() {
        return serviceKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
