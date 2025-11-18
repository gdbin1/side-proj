// com.gbk.sideproj.config.RestTemplateConfig.java 수정

package com.gbk.sideproj.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(200)) 
                .setReadTimeout(Duration.ofSeconds(200))
                .defaultHeader("Accept", "application/xml")
                .build();
    }
}