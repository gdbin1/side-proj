package com.gbk.sideproj.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DetailService {

	@Value("${api.url}")
	private String apiUrl;

	@Value("${api.serviceKey}")
	private String apiKey;


    public Map<String, String> getDetail(String plnmNo, String pbctNo) {

        try {
            String url = apiUrl
                    + "?serviceKey=" + apiKey
                    + "&pageNo=1"
                    + "&numOfRows=10"
                    + "&type=xml"
                    + "&PLNM_NO=" + plnmNo
                    + "&PBCT_NO=" + pbctNo;

            RestTemplate rt = new RestTemplate();
            String xml = rt.getForObject(url, String.class);

            if (xml == null || xml.isBlank()) {
                return null;
            }

            if (!xml.contains("<item>")) {
                return null;
            }

            Map<String, String> result = new LinkedHashMap<>();

            extract(xml, "CLTR_NM", "물건명", result);
            extract(xml, "LDNM_ADRS", "주소", result);
            extract(xml, "APSL_ASES_AVG_AMT", "감정가", result);
            extract(xml, "MIN_BID_PRC", "최저입찰가", result);
            extract(xml, "PBCT_CLTR_STAT_NM", "진행상태", result);
            extract(xml, "PBCT_BEGN_DTM", "입찰 시작", result);
            extract(xml, "PBCT_CLS_DTM", "입찰 마감", result);

            // 원래 WebItem에 있던 요약데이터도 표시
            result.put("공고번호", plnmNo);
            result.put("공매번호", pbctNo);

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    private void extract(String xml, String tag, String label, Map<String, String> map) {
        try {
            String open = "<" + tag + ">";
            String close = "</" + tag + ">";

            if (!xml.contains(open)) return;

            String value = xml.substring(xml.indexOf(open) + open.length(), xml.indexOf(close)).trim();
            if (value.isBlank()) return;

            map.put(label, value);
        } catch (Exception ignore) {}
    }
}
