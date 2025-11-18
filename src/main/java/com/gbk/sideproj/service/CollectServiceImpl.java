package com.gbk.sideproj.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;

import com.gbk.sideproj.domain.WebItem;
import com.gbk.sideproj.domain.WebItemDetail;
import com.gbk.sideproj.mapper.WebItemMapper;
import com.gbk.sideproj.mapper.WebItemDetailMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private final WebItemMapper webItemMapper;
    private final WebItemDetailMapper detailMapper;

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 지원하는 날짜 포맷들 (우선순위대로 시도)
    private static final DateTimeFormatter DTF_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DTF_STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 상세 수집 관련 상수
    private static final int MAX_TOTAL_ITEMS_TO_FETCH = 15; // 목록으로 저장할 최대 개수
    private static final int DETAIL_NUM_PER_PAGE = 100; // API 요청 시 numOfRows
    private static final int MAX_DETAIL_PAGES = 20; // 상세 수집 시 검색할 최대 페이지 수 (안전장치)
    private static final int MAX_DETAILS_PER_GROUP = 3; // 각 목록 항목당 저장할 최대 상세 개수 (최근순)

    // ====================================================================================
    // 메인: 2~3 페이지에서 전체 물건 중 15개 수집 + webItem 및 webItemDetail DB 최신화
    // ====================================================================================
    @Override
    @Transactional
    public int collectAndSave() {

        log.info("📌 [수집 시작] 2~3 페이지에서 {}개 아이템 수집 (목록) 및 상세(최대 {}건/항목) 저장",
                MAX_TOTAL_ITEMS_TO_FETCH, MAX_DETAILS_PER_GROUP);

        List<WebItem> collected = new ArrayList<>();

        // 2~3페이지에서 데이터 수집 (필터 없음)
        for (int pageNo = 2; pageNo <= 3; pageNo++) {
            List<WebItem> pageItems = fetchItems(pageNo);

            if (pageItems != null && !pageItems.isEmpty()) {
                collected.addAll(pageItems);
            }

            // 이미 충분히 모였으면 더 가져올 필요 없음 (안전)
            if (collected.size() >= MAX_TOTAL_ITEMS_TO_FETCH)
                break;
        }

        // 중복 제거 기준: cltrNm(물건명) — 필요하면 주소까지 키 확장 가능
        Map<String, WebItem> unique = new LinkedHashMap<>();
        for (WebItem it : collected) {
            if (it == null) continue;
            String key = safeString(it.getCltrNm()).trim();
            if (!unique.containsKey(key) && !key.isBlank()) {
                unique.put(key, it);
            }
        }

        List<WebItem> finalList = new ArrayList<>(unique.values());

        // 최대 개수 제한
        if (finalList.size() > MAX_TOTAL_ITEMS_TO_FETCH) {
            finalList = finalList.subList(0, MAX_TOTAL_ITEMS_TO_FETCH);
        }

        log.info("📌 [정제 완료] 최종 목록 개수: {}", finalList.size());

        // DB 비우기 & 새 데이터 삽입 (webItem)
        webItemMapper.deleteAll();
        if (!finalList.isEmpty()) {
            webItemMapper.insertItems(finalList);
        }
        log.info("📌 [저장 완료] webItem {}개 저장", finalList.size());

        // ------------------------------------------
        // webItemDetail 수집 및 저장 (각 webItem에 대해 최대 최근 3건)
        // ------------------------------------------
        int totalDetailInserted = 0;

        // 요구사항: webItem에 해당하는 상세만 유지하기 위해 전체 또는 키별로 삭제 처리
        // 간단하게 전체 삭제 후 삽입 (작업 단순화). 필요하면 deleteByKeys로 변경 가능.
        try {
            detailMapper.deleteAll();
        } catch (Exception ex) {
            log.warn("webItemDetail 전체 삭제 실패: {}", ex.getMessage());
        }

        for (WebItem item : finalList) {
            if (item == null) continue;
            String targetCltrNm = safeString(item.getCltrNm());
            String targetLdnAdrs = safeString(item.getLdnAdrs());

            // 검색해서 매칭되는 상세들을 수집 (페이지 순회)
            List<WebItemDetail> found = fetchDetailsForKey(targetCltrNm, targetLdnAdrs, MAX_DETAILS_PER_GROUP);

            // 삽입
            for (WebItemDetail d : found) {
                try {
                    detailMapper.insert(d);
                    totalDetailInserted++;
                } catch (Exception ex) {
                    log.warn("webItemDetail insert 실패 (plnmNo={}, pbctNo={}): {}", d.getPlnmNo(), d.getPbctNo(), ex.getMessage());
                }
            }

            log.info("→ '{}' / '{}' 에 대해 {}건 삽입", targetCltrNm, targetLdnAdrs, found.size());
        }

        log.info("📌 [상세 저장 완료] 총 {}건의 webItemDetail 삽입", totalDetailInserted);

        return finalList.size();
    }

    // ====================================================================================
    // 상세: 특정 (cltrNm, ldnmAdrs) 키로 API 전체 검색하여 매칭되는 항목을 모아서 최신순으로 최대 n개 반환
    // - 검색은 페이지 단위로 진행하며 MAX_DETAIL_PAGES 까지 시도
    // ====================================================================================
    private List<WebItemDetail> fetchDetailsForKey(String cltrNm, String ldnAdrs, int maxResults) {
        List<WebItemDetail> acc = new ArrayList<>();
        if (isBlank(cltrNm)) return acc;

        String normTargetName = normalizeForMatch(cltrNm);
        String normTargetAddr = normalizeForMatch(ldnAdrs);

        for (int page = 1; page <= MAX_DETAIL_PAGES; page++) {
            try {
                String url = baseUrl
                        + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)
                        + "&pageNo=" + page
                        + "&numOfRows=" + DETAIL_NUM_PER_PAGE
                        + "&type=xml";

                URI uri = new URI(url);
                String xml = restTemplate.getForObject(uri, String.class);
                if (xml == null || xml.isBlank()) continue;

                // 파싱해서 item nodes 돌면서 매칭되는 것들 수집
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                NodeList itemNodes = doc.getElementsByTagName("item");
                if (itemNodes.getLength() == 0) itemNodes = doc.getElementsByTagName("ITEM");

                if (itemNodes.getLength() == 0) {
                    // 더 이상 결과 없을 가능성
                    break;
                }

                for (int i = 0; i < itemNodes.getLength(); i++) {
                    Node n = itemNodes.item(i);
                    if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                    Element e = (Element) n;

                    String nodeCltrNm = getTextIgnoreCase(e, "CLTR_NM", "cltrNm", "goodsNm", "GOODS_NM");
                    String nodeLdnAdrs = getTextIgnoreCase(e, "LDNM_ADRS", "ldnmAdrs", "ldnAdrs", "LDNM_ADRS");

                    if (nodeCltrNm == null) continue;

                    String normNodeName = normalizeForMatch(nodeCltrNm);
                    String normNodeAddr = normalizeForMatch(nodeLdnAdrs);

                    // 매칭 기준: 목록의 cltrNm 포함(또는 동일) 및 주소 포함(주소가 비어있으면 이름만 매칭)
                    boolean nameMatches = normNodeName.contains(normTargetName) || normTargetName.contains(normNodeName);
                    boolean addrMatches = true;
                    if (!isBlank(normTargetAddr) && !isBlank(normNodeAddr)) {
                        addrMatches = normNodeAddr.contains(normTargetAddr) || normTargetAddr.contains(normNodeAddr);
                    }

                    if (nameMatches && addrMatches) {
                        WebItemDetail detail = parseDetailFromElement(e);
                        // 보정: plnmNo/pbctNo가 비어있지 않으면 설정
                        if (isBlank(detail.getPlnmNo()) || isBlank(detail.getPbctNo())) {
                            String plnm = getTextIgnoreCase(e, "PLNM_NO", "plnmNo", "plnm_no", "plnmno");
                            String pbct = getTextIgnoreCase(e, "PBCT_NO", "pbctNo", "pbct_no", "pbctno");
                            if (!isBlank(plnm)) detail.setPlnmNo(plnm);
                            if (!isBlank(pbct)) detail.setPbctNo(pbct);
                        }
                        acc.add(detail);
                    }
                }

                // 이미 충분히 수집했다면 중단 가능 (단, 정렬 필요)
                if (acc.size() >= maxResults * 3) {
                    // 어느 정도 여유를 두고 모았다가 나중에 정렬해서 잘라냄
                    break;
                }

            } catch (Exception ex) {
                log.warn("fetchDetailsForKey 페이지 {} 처리 중 오류: {}", page, ex.getMessage());
            }
        }

        // 수집된 항목들을 pbctBegnDtm(입찰 시작일) 기준으로 내림차순 정렬
        List<WebItemDetail> sorted = acc.stream()
                .sorted((a, b) -> {
                    LocalDateTime da = parseDateFlexibleNullable(a.getPbctBegnDtm());
                    LocalDateTime db = parseDateFlexibleNullable(b.getPbctBegnDtm());
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da); // 내림차순 (최신 먼저)
                })
                .collect(Collectors.toList());

        // 중복(plnmNo+pbctNo) 제거하며 최대 maxResults개 선택
        List<WebItemDetail> result = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (WebItemDetail d : sorted) {
            String key = safeString(d.getPlnmNo()) + "||" + safeString(d.getPbctNo());
            if (seenKeys.contains(key)) continue;
            seenKeys.add(key);
            // 필수 필드 채우기: plnmNo/pbctNo가 비어있으면 건너뛰기
            if (isBlank(d.getPlnmNo()) || isBlank(d.getPbctNo())) {
                continue;
            }
            result.add(d);
            if (result.size() >= maxResults) break;
        }

        return result;
    }

    // ====================================================================================
    // XML Element -> WebItemDetail 매핑
    // ====================================================================================
    private WebItemDetail parseDetailFromElement(Element e) {
        WebItemDetail detail = new WebItemDetail();
        // 핵심키
        detail.setPlnmNo(getTextIgnoreCase(e, "PLNM_NO", "plnmNo", "plnm_no", "plnmno"));
        detail.setPbctNo(getTextIgnoreCase(e, "PBCT_NO", "pbctNo", "pbct_no", "pbctno"));

        // 나머지 필드들 (가능한 케이스들 고려)
        detail.setRnum(parseIntSafe(getTextIgnoreCase(e, "RNUM")));
        detail.setPbctCdtnNo(getTextIgnoreCase(e, "PBCT_CDTN_NO", "pbctCdtnNo"));
        detail.setCltrNo(getTextIgnoreCase(e, "CLTR_NO", "cltrNo"));
        detail.setCltrHstrNo(getTextIgnoreCase(e, "CLTR_HSTR_NO", "cltrHstrNo"));
        detail.setScrnGrpCd(getTextIgnoreCase(e, "SCRN_GRP_CD", "scrnGrpCd"));
        detail.setCtgrFullNm(getTextIgnoreCase(e, "CTGR_FULL_NM", "ctgrFullNm"));
        detail.setBidMnmtNo(getTextIgnoreCase(e, "BID_MNMT_NO", "bidMnmtNo"));
        detail.setCltrNm(getTextIgnoreCase(e, "CLTR_NM", "cltrNm", "GOODS_NM", "goodsNm"));
        detail.setCltrMnmtNo(getTextIgnoreCase(e, "CLTR_MNMT_NO", "cltrMnmtNo"));
        detail.setLdnmAdrs(getTextIgnoreCase(e, "LDNM_ADRS", "ldnmAdrs", "ldnAdrs"));
        detail.setNmrdAdrs(getTextIgnoreCase(e, "NMRD_ADRS", "nmrdAdrs"));
        detail.setLdnmPnu(getTextIgnoreCase(e, "LDNM_PNU", "ldnmPnu"));
        detail.setDpslMtdCd(getTextIgnoreCase(e, "DPSL_MTD_CD", "dpslMtdCd"));
        detail.setDpslMtdNm(getTextIgnoreCase(e, "DPSL_MTD_NM", "dpslMtdNm"));
        detail.setBidMtdNm(getTextIgnoreCase(e, "BID_MTD_NM", "bidMtdNm"));
        detail.setMinBidPrc(parseLongSafe(getTextIgnoreCase(e, "MIN_BID_PRC", "minBidPrc")));
        detail.setApslAsesAvgAmt(parseLongSafe(getTextIgnoreCase(e, "APSL_ASES_AVG_AMT", "apslAsesAvgAmt")));
        detail.setFeeRate(getTextIgnoreCase(e, "FEE_RATE", "feeRate"));
        detail.setPbctBegnDtm(getTextIgnoreCase(e, "PBCT_BEGN_DTM", "pbctBegnDtm"));
        detail.setPbctClsDtm(getTextIgnoreCase(e, "PBCT_CLS_DTM", "pbctClsDtm"));
        detail.setPbctCltrStatNm(getTextIgnoreCase(e, "PBCT_CLTR_STAT_NM", "pbctCltrStatNm"));
        detail.setUscbCnt(parseIntSafe(getTextIgnoreCase(e, "USCB_CNT", "uscbCnt")));
        detail.setIqryCnt(parseIntSafe(getTextIgnoreCase(e, "IQRY_CNT", "iqryCnt")));
        detail.setGoodsNm(getTextIgnoreCase(e, "GOODS_NM", "goodsNm"));
        detail.setCltrImgFiles(getTextIgnoreCase(e, "CLTR_IMG_FILES", "cltrImgFiles"));

        return detail;
    }

    // ====================================================================================
    // 기존 fetchItems / parseItems (목록 수집) - 거의 동일
    // ====================================================================================
    private List<WebItem> fetchItems(int pageNo) {
        try {
            // 반드시 XML 응답을 받도록 type=xml 추가
            String url = baseUrl + "?serviceKey=" + URLEncoder.encode(serviceKey, StandardCharsets.UTF_8) + "&pageNo="
                    + pageNo + "&numOfRows=100" + "&type=xml";

            log.info("🌐 API 요청 → page {} : {}", pageNo, url);

            URI uri = new URI(url);
            String xml = restTemplate.getForObject(uri, String.class);

            if (xml == null || xml.isEmpty()) {
                log.warn("⚠ API 응답 비었음 (page {})", pageNo);
                return Collections.emptyList();
            }

            log.debug("[RAW XML length={}] (page {})", xml.length(), pageNo);
            return parseItems(xml);

        } catch (Exception e) {
            log.error("❌ fetchItems 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<WebItem> parseItems(String xml) {
        List<WebItem> list = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList itemNodes = doc.getElementsByTagName("item");
            if (itemNodes.getLength() == 0)
                itemNodes = doc.getElementsByTagName("ITEM"); // 대문자 케이스

            log.info("🔎 XML 파싱: itemNodes length = {}", itemNodes.getLength());

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                Element e = (Element) node;

                // 케이스/형식이 다양한 태그명 후보들
                String plnmNo = getTextIgnoreCase(e, "PLNM_NO", "plnmNo", "plnm_no", "plnmno");
                String pbctNo = getTextIgnoreCase(e, "PBCT_NO", "pbctNo", "pbct_no", "pbctno");
                String cltrNm = getTextIgnoreCase(e, "CLTR_NM", "cltrNm", "cltr_nm", "cltrnm", "GOODS_NM", "goodsNm");
                String ldnAdrs = getTextIgnoreCase(e, "LDNM_ADRS", "ldnmAdrs", "ldnAdrs", "ldn_adrs", "ldnm_addr",
                        "ldnAddr");
                String apslAses = getTextIgnoreCase(e, "APSL_ASES_AVG_AMT", "apslAsesAmt", "APSL_ASES",
                        "apsl_ases_avg_amt");
                String minBid = getTextIgnoreCase(e, "MIN_BID_PRC", "minBidPrc", "min_bid_prc", "minBid");
                String pbctStat = getTextIgnoreCase(e, "PBCT_CLTR_STAT_NM", "pbctStatNm", "pbct_cltr_stat_nm",
                        "pbct_stat", "pbctCltrStatNm");
                String imgFiles = getTextIgnoreCase(e, "CLTR_IMG_FILES", "cltrImgFiles", "cltr_img_files", "CLTR_IMG");
                String pbctBegn = getTextIgnoreCase(e, "PBCT_BEGN_DTM", "pbctBegnDtm", "pbct_begn_dtm", "PBCT_BEGN");
                String pbctCls = getTextIgnoreCase(e, "PBCT_CLS_DTM", "pbctClsDtm", "pbct_cls_dtm", "PBCT_CLS");

                // 필수키 검사: plnmNo, pbctNo, cltrNm 없으면 스킵
                if (isBlank(plnmNo) || isBlank(pbctNo)) {
                    log.warn("[SKIP] 필수키(plnmNo/pbctNo) 누락: plnmNo='{}' pbctNo='{}' (index={})", plnmNo, pbctNo, i);
                    continue;
                }
                if (isBlank(cltrNm)) {
                    log.warn("[SKIP] cltrNm 누락 (NOT NULL 제약) plnmNo='{}' pbctNo='{}' (index={})", plnmNo, pbctNo, i);
                    continue;
                }

                WebItem item = new WebItem();
                item.setPlnmNo(plnmNo.trim());
                item.setPbctNo(pbctNo.trim());
                item.setCltrNm(cltrNm.trim());
                item.setLdnAdrs(isBlank(ldnAdrs) ? "" : ldnAdrs.trim());
                item.setApslAsesAmt(parseLongSafe(apslAses));
                item.setMinBidPrc(parseLongSafe(minBid));
                item.setPbctStatNm(isBlank(pbctStat) ? "" : pbctStat.trim());
                item.setImgUrl(isBlank(imgFiles) ? "" : imgFiles.trim());
                item.setOnbdUrl(""); // 필요하면 조합해서 넣을 수 있음

                // 날짜는 도메인 타입에 맞춰 넣어주세요 (현재 parseDateFlexible은 LocalDateTime 반환)
                item.setPbctBegnDtm(parseDateFlexible(pbctBegn));
                item.setPbctClsDtm(parseDateFlexible(pbctCls));

                log.debug("→ Parsed item: plnmNo={} pbctNo={} cltrNm={} minBid={} apsl={}", item.getPlnmNo(),
                        item.getPbctNo(), item.getCltrNm(), item.getMinBidPrc(), item.getApslAsesAmt());

                list.add(item);
            }

        } catch (Exception e) {
            log.error("❌ XML 파싱 실패: {}", e.getMessage(), e);
        }

        return list;
    }

    // ====================================================================================
    // 헬퍼 메소드들 (기존 코드 재사용 + 몇가지 보조 메서드 추가)
    // ====================================================================================
    private String getTextIgnoreCase(Element el, String... candidates) {
        if (el == null) return null;

        // 1) 빠른 경로: getElementsByTagName 후보들 직접 체크
        for (String cand : candidates) {
            if (cand == null) continue;
            NodeList nl = el.getElementsByTagName(cand);
            if (nl != null && nl.getLength() > 0) {
                String v = nl.item(0).getTextContent();
                if (v != null && !v.isBlank()) return v.trim();
            }
        }

        // 2) 범용 경로: 모든 자식 노드 순회하며 이름을 대소문자 무시해서 비교
        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            String nodeName = n.getNodeName();
            for (String cand : candidates) {
                if (nodeName.equalsIgnoreCase(cand)) {
                    String v = n.getTextContent();
                    if (v != null && !v.isBlank()) return v.trim();
                }
                if (normalizeName(nodeName).equalsIgnoreCase(normalizeName(cand))) {
                    String v = n.getTextContent();
                    if (v != null && !v.isBlank()) return v.trim();
                }
            }
        }
        return null;
    }

    private String normalizeName(String s) {
        if (s == null) return "";
        return s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    private String normalizeForMatch(String s) {
        if (s == null) return "";
        // 공백/특수문자 제거, 소문자화 — 검색/매칭용
        return s.replaceAll("[\\s\\p{Punct}]+", "").toLowerCase();
    }

    private Integer parseIntSafe(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.replaceAll("[^0-9\\-]", "")); }
        catch (Exception e) { return null; }
    }

    private Long parseLongSafe(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[^0-9\\-]", "");
        if (cleaned.isBlank()) return null;
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException ex) {
            log.debug("숫자 파싱 실패: '{}'", s);
            return null;
        }
    }

    private LocalDateTime parseDateFlexible(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        try {
            if (t.matches("\\d{14}")) {
                return LocalDateTime.parse(t, DTF_YYYYMMDDHHMMSS);
            }
        } catch (DateTimeParseException ignored) {}
        try {
            return LocalDateTime.parse(t, DTF_STANDARD);
        } catch (DateTimeParseException ignored) {}

        String digits = t.replaceAll("[^0-9]", "");
        if (digits.length() >= 14) {
            try {
                return LocalDateTime.parse(digits.substring(0, 14), DTF_YYYYMMDDHHMMSS);
            } catch (DateTimeParseException ignored) {}
        }

        log.debug("날짜 파싱 불가: '{}'", s);
        return null;
    }

    private LocalDateTime parseDateFlexibleNullable(String s) {
        return parseDateFlexible(s);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }
}
