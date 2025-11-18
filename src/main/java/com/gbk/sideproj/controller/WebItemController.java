package com.gbk.sideproj.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gbk.sideproj.mapper.WebItemMapper;
import com.gbk.sideproj.domain.WebItem;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebItemController {

    private final WebItemMapper webItemMapper;

    // ================== 리스트 페이지 ==================
    @GetMapping("/items")
    public String items(Model model) {
        model.addAttribute("items", webItemMapper.findAll());
        return "items";
    }

    // ================== 기존 상세정보 조회 ==================
    @GetMapping("/api/detail")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getDetail(
            @RequestParam String plnmNo,
            @RequestParam String pbctNo
    ) {
        Map<String, String> result = new LinkedHashMap<>();
        List<WebItem> items = webItemMapper.findByPkList(plnmNo, pbctNo);
        if (!items.isEmpty()) {
            WebItem item = items.get(0);
            putIfNotEmpty(result, "공매명", safeTrim(item.getCltrNm()));
            putIfNotEmpty(result, "감정가", formatCurrency(item.getApslAsesAmt()));
            putIfNotEmpty(result, "최저입찰가", formatCurrency(item.getMinBidPrc()));
            putIfNotEmpty(result, "주소", safeTrim(item.getLdnAdrs()));
            putIfNotEmpty(result, "이미지URL", safeTrim(item.getImgUrl()));
            putIfNotEmpty(result, "공매상태", safeTrim(item.getPbctStatNm()));
            putIfNotEmpty(result, "공매시작일", formatDateTime(item.getPbctBegnDtm()));
            putIfNotEmpty(result, "공매종료일", formatDateTime(item.getPbctClsDtm()));
        }
        return ResponseEntity.ok(result);
    }

    // ================== 팝업용 상세정보 조회 ==================
    @GetMapping("/api/popupDetail")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPopupDetail(
            @RequestParam String cltrNm
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 기존 상세정보
        List<Map<String, Object>> rawResults = webItemMapper.findItemWithDetails(cltrNm);
        if (!rawResults.isEmpty()) {
            Map<String, Object> item = rawResults.get(0); // 대표 상세정보

            if (item.get("cltrNm") != null) result.put("cltrNm", safeTrim(item.get("cltrNm")));
            if (item.get("ldnAdrs") != null) result.put("ldnAdrs", safeTrim(item.get("ldnAdrs")));
            if (item.get("apslAsesAmt") != null) result.put("apslAsesAmt", formatCurrency(item.get("apslAsesAmt")));
            if (item.get("minBidPrc") != null) result.put("minBidPrc", formatCurrency(item.get("minBidPrc")));
            if (item.get("pbctStatNm") != null) result.put("pbctStatNm", safeTrim(item.get("pbctStatNm")));
            if (item.get("ctgrFullNm") != null) result.put("ctgrFullNm", safeTrim(item.get("ctgrFullNm")));
            if (item.get("nmrdAdrs") != null) result.put("nmrdAdrs", safeTrim(item.get("nmrdAdrs")));
            if (item.get("dpslMtdNm") != null) result.put("dpslMtdNm", safeTrim(item.get("dpslMtdNm")));
            if (item.get("bidMtdNm") != null) result.put("bidMtdNm", safeTrim(item.get("bidMtdNm")));
            if (item.get("apslAsesAvgAmt") != null) result.put("apslAsesAvgAmt", formatCurrency(item.get("apslAsesAvgAmt")));
            if (item.get("feeRate") != null) result.put("feeRate", safeTrim(item.get("feeRate")));
            if (item.get("goodsNm") != null) result.put("goodsNm", safeTrim(item.get("goodsNm")));
            if (item.get("cltrImgFiles") != null) result.put("cltrImgFiles", safeTrim(item.get("cltrImgFiles")));
        }

        // ================== 이력 내역 최근 3건 ==================
        List<Map<String, Object>> history = rawResults.stream()
                .sorted((a, b) -> b.get("pbctBegnDtm").toString().compareTo(a.get("pbctBegnDtm").toString())) // 최신 순
                .limit(3)
                .map(h -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    if (h.get("pbctBegnDtm") != null) map.put("pbctBegnDtm", formatDateTime(h.get("pbctBegnDtm")));
                    if (h.get("pbctClsDtm") != null) map.put("pbctClsDtm", formatDateTime(h.get("pbctClsDtm")));
                    if (h.get("minBidPrc") != null) map.put("minBidPrc", formatCurrency(h.get("minBidPrc")));
                    return map;
                })
                .collect(Collectors.toList());

        result.put("history", history);

        return ResponseEntity.ok(result);
    }

    // ================== 헬퍼 메서드 ==================
    private void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }

    private String safeTrim(Object o) {
        if (o == null) return null;
        return o.toString().trim();
    }

    private String formatCurrency(Object o) {
        if (o == null) return null;
        try {
            long n = Long.parseLong(o.toString());
            return String.format("%,d원", n);
        } catch (Exception e) {
            return o.toString();
        }
    }

    private String formatDateTime(Object dt) {
        if (dt == null) return null;
        if (dt instanceof String) return ((String) dt).trim();
        try {
            java.time.LocalDateTime ldt = (java.time.LocalDateTime) dt;
            return ldt.toString().replace('T', ' ');
        } catch (Exception ignored) {}
        return dt.toString();
    }
}
