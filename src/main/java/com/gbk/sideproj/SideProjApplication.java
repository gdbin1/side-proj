package com.gbk.sideproj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.gbk.sideproj", "com.sideproj.test"})
@MapperScan(basePackages = {"com.gbk.sideproj.mapper"})
public class SideProjApplication {

	public static void main(String[] args) {
		SpringApplication.run(SideProjApplication.class, args);
	}
	
//	@Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
}
