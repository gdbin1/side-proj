package com.gbk.sideproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gbk.sideproj.mapper.WebItemMapper;
import com.gbk.sideproj.service.CollectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CollectService collectService; // ★ 통합된 단일 CollectService
    private final WebItemMapper webItemMapper;

    // 관리자 페이지
    @GetMapping
    public String adminPage(Model model) {
        int count = webItemMapper.findAll().size();
        model.addAttribute("itemCount", count);
        return "admin";
    }

    // ================================
    // ★ 1) 단일 통합 데이터 수집
    //    - webItem (14개 중복 없는 데이터)
    //    - webItemDetail (각 물건별 최대 3개)
    // ================================
    @PostMapping(value = "/collect-all", produces = "text/plain; charset=UTF-8")
    @ResponseBody
    public String collectAll() {
        try {
            log.info("📌 [ADMIN] 통합 데이터 수집 시작");

            // CollectService 내부에서
            // 1) webItem 수집
            // 2) webItemDetail 수집 (3건 제한 유지)
            // 이 로직을 모두 수행하도록 이미 구성됨.
            int detailCount = collectService.collectAndSave(); 

            log.info("📌 [ADMIN] 통합 데이터 수집 완료 — 상세 {}건 저장", detailCount);

            return "✅ 통합 데이터 수집 완료 (" + detailCount + "건 상세 저장)";
        } catch (Exception e) {
            log.error("❌ 통합 데이터 수집 오류", e);
            return "❌ 오류 발생: " + e.getMessage();
        }
    }
}
