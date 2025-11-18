package com.gbk.sideproj.service;

import com.gbk.sideproj.domain.WebItem;
import com.gbk.sideproj.domain.WebItemDetail;
import com.gbk.sideproj.mapper.WebItemDetailMapper;
import com.gbk.sideproj.mapper.WebItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.xml.sax.InputSource;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectDetailServiceImpl implements CollectDetailService {

    private final WebItemMapper webItemMapper;
    private final WebItemDetailMapper detailMapper;

    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${api.url}")
    private String baseUrl;

    @Override
    public int collectAllDetails() {

        List<WebItem> items = webItemMapper.findAll();
        if (items == null || items.isEmpty()) {
            log.info("📌 collectAllDetails: 수집할 webItem이 없습니다.");
            return 0;
        }

        RestTemplate rt = new RestTemplate();
        int totalSaved = 0;

        for (WebItem item : items) {
            String plnmNo = item == null ? null : item.getPlnmNo();
            String pbctNo = item == null ? null : item.getPbctNo();
            if (plnmNo == null || pbctNo == null) {
                log.warn("🔎 건너뜀: plnmNo 또는 pbctNo 없음 (item id={})", item == null ? "null" : item.getPlnmNo());
                continue;
            }

            try {
                // URL 안전 빌드
                String url = new StringBuilder()
                        .append(baseUrl)
                        .append("?serviceKey=").append(URLEncoder.encode(serviceKey == null ? "" : serviceKey, StandardCharsets.UTF_8))
                        .append("&pageNo=1&numOfRows=100&type=xml")
                        .append("&PLNM_NO=").append(URLEncoder.encode(plnmNo, StandardCharsets.UTF_8))
                        .append("&PBCT_NO=").append(URLEncoder.encode(pbctNo, StandardCharsets.UTF_8))
                        .toString();

                log.debug("🌐 상세 API 호출: plnmNo={} pbctNo={} url={}", plnmNo, pbctNo, url);

                String xml = rt.getForObject(url, String.class);
                if (xml == null || xml.isBlank()) {
                    log.warn("⚠ 상세 API 응답 비어있음: plnmNo={}, pbctNo={}", plnmNo, pbctNo);
                    continue;
                }

                // 안전한 XML 파싱 (기본 XXE 방지 설정)
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(false);
                try {
                    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                } catch (Exception ignored) {}
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(xml)));
                doc.getDocumentElement().normalize();

                NodeList itemNodes = doc.getElementsByTagName("item");
                if (itemNodes.getLength() == 0) itemNodes = doc.getElementsByTagName("ITEM");
                if (itemNodes.getLength() == 0) {
                    log.debug("🔎 상세 item 노드 없음: plnmNo={}, pbctNo={}", plnmNo, pbctNo);
                    continue;
                }

                List<WebItemDetail> details = new ArrayList<>();

                for (int idx = 0; idx < itemNodes.getLength(); idx++) {
                    Node node = itemNodes.item(idx);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                    NodeList children = node.getChildNodes();
                    WebItemDetail detail = new WebItemDetail();
                    detail.setPlnmNo(plnmNo);
                    detail.setPbctNo(pbctNo);

                    for (int i = 0; i < children.getLength(); i++) {
                        Node c = children.item(i);
                        if (c.getNodeType() != Node.ELEMENT_NODE) continue;

                        String tag = c.getNodeName();
                        String value = c.getTextContent() == null ? "" : c.getTextContent().trim();
                        if (value.isEmpty()) continue;

                        switch (tag.toUpperCase()) {
                            case "RNUM": detail.setRnum(parseIntSafe(value)); break;
                            case "PBCT_CDTN_NO": detail.setPbctCdtnNo(value); break;
                            case "CLTR_NO": detail.setCltrNo(value); break;
                            case "CLTR_HSTR_NO": detail.setCltrHstrNo(value); break;
                            case "SCRN_GRP_CD": detail.setScrnGrpCd(value); break;
                            case "CTGR_FULL_NM": detail.setCtgrFullNm(value); break;
                            case "BID_MNMT_NO": detail.setBidMnmtNo(value); break;
                            case "CLTR_NM": detail.setCltrNm(value); break;
                            case "CLTR_MNMT_NO": detail.setCltrMnmtNo(value); break;
                            case "LDNM_ADRS": detail.setLdnmAdrs(value); break;
                            case "NMRD_ADRS": detail.setNmrdAdrs(value); break;
                            case "LDNM_PNU": detail.setLdnmPnu(value); break;
                            case "DPSL_MTD_CD": detail.setDpslMtdCd(value); break;
                            case "DPSL_MTD_NM": detail.setDpslMtdNm(value); break;
                            case "BID_MTD_NM": detail.setBidMtdNm(value); break;
                            case "MIN_BID_PRC": detail.setMinBidPrc(parseLongSafe(value)); break;
                            case "APSL_ASES_AVG_AMT": detail.setApslAsesAvgAmt(parseLongSafe(value)); break;
                            case "FEE_RATE": detail.setFeeRate(value); break;
                            case "PBCT_BEGN_DTM": detail.setPbctBegnDtm(value); break;
                            case "PBCT_CLS_DTM": detail.setPbctClsDtm(value); break;
                            case "PBCT_CLTR_STAT_NM": detail.setPbctCltrStatNm(value); break;
                            case "USCBD_CNT":
                            case "USCB_CNT": detail.setUscbCnt(parseIntSafe(value)); break;
                            case "IQRY_CNT": detail.setIqryCnt(parseIntSafe(value)); break;
                            case "GOODS_NM": detail.setGoodsNm(value); break;
                            case "CLTR_IMG_FILES": detail.setCltrImgFiles(value); break;
                            default:
                                // 기타 태그는 현재 저장하지 않음
                                break;
                        }
                    }

                    details.add(detail);
                }

                // pbctBegnDtm 숫자 기준으로 최신순 정렬 (안정적 비교)
                details.sort((a, b) -> {
                    String da = extractDigits(a.getPbctBegnDtm());
                    String db = extractDigits(b.getPbctBegnDtm());
                    // 길이 맞추기 (앞쪽 0패딩) — 비교 안전성 확보
                    if (da.length() < db.length()) da = padLeft(da, db.length());
                    if (db.length() < da.length()) db = padLeft(db, da.length());
                    return db.compareTo(da);
                });

                List<WebItemDetail> top3 = details.size() > 3 ? details.subList(0, 3) : details;

                // 기존 상세 삭제 (해당 키만)
                try {
                    detailMapper.deleteByKeys(plnmNo, pbctNo);
                } catch (Exception ex) {
                    log.warn("삭제 실패 (계속 진행): plnmNo={}, pbctNo={}, err={}", plnmNo, pbctNo, ex.getMessage());
                }

                // insert (개별 실패 무시)
                int savedThis = 0;
                for (WebItemDetail d : top3) {
                    try {
                        detailMapper.insert(d);
                        savedThis++;
                    } catch (Exception ex) {
                        log.warn("상세 insert 실패 (계속 진행): plnmNo={}, pbctNo={}, err={}", plnmNo, pbctNo, ex.getMessage());
                    }
                }

                totalSaved += savedThis;
                log.debug("✅ 상세 처리 완료: plnmNo={} pbctNo={} saved={}", plnmNo, pbctNo, savedThis);

            } catch (Exception e) {
                log.error("❌ collectAllDetails 예외: plnmNo={}, pbctNo={}, err={}", plnmNo, pbctNo, e.getMessage(), e);
            }
        }

        log.info("📌 collectAllDetails 완료 — 총 저장된 상세 건수: {}", totalSaved);
        return totalSaved;
    }

    private Integer parseIntSafe(String s) {
        try {
            String cleaned = s == null ? "" : s.replaceAll("[^0-9\\-]", "");
            if (cleaned.isBlank()) return null;
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLongSafe(String s) {
        try {
            String cleaned = s == null ? "" : s.replaceAll("[^0-9\\-]", "");
            if (cleaned.isBlank()) return null;
            return Long.parseLong(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractDigits(String s) {
        if (s == null) return "";
        String d = s.replaceAll("[^0-9]", "");
        return d;
    }

    private String padLeft(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len - s.length(); i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }
}
