package com.gbk.sideproj.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gbk.sideproj.domain.WebItem;
import com.gbk.sideproj.mapper.WebItemMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "WebItem API", description = "공매 데이터 조회 REST API")
@RestController
@RequestMapping("/api/webitem")
public class WebItemApiController {

    private final WebItemMapper webItemMapper;

    public WebItemApiController(WebItemMapper webItemMapper) {
        this.webItemMapper = webItemMapper;
    }

    @Operation(
        summary = "공매 아이템 전체 조회",
        description = "DB에 저장된 경기도 공매 Top15 아이템을 JSON 형태로 반환합니다."
    )
    @GetMapping("/list")
    public List<WebItem> findAll() {
        return webItemMapper.findAll();
    }
}
// 스웨거 주소--------------------------------------