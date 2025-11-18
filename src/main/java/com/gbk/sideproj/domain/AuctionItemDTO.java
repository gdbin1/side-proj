// com.gbk.sideproj.domain.AuctionItemDTO.java

package com.gbk.sideproj.domain;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class AuctionItemDTO {
	// rNum은 DB 저장용이 아니므로 제외합니다.
    
    // CLTR_NM: 물건명 (API 필드명: CLTR_NM)
    private String cltrNm;
    
    // MIN_BID_PRC: 최저입찰가 (API 필드명: MIN_BID_PRC)
    private Long minBidPrc; 
    
    // APSL_ASES_AVG_AMT: 감정가 (API 필드명: APSL_ASES_AVG_AMT)
    private Long apslAsesAmt; 
    
    // PBCT_BEGN_DTM: 공고 시작일시 (API 필드명: PBCT_BEGN_DTM)
    // LocalDateTime으로 받아야 추후 포맷팅이 용이하므로 타입 변경
    private LocalDateTime pbctBegnDtm; 
    
    // LDNM_ADRS: 지번 주소 (API 필드명: LDNM_ADRS)
    private String ldnAdrs;
    
    // CTGR_FULL_NM: 카테고리 (API 필드명: CTGR_FULL_NM)
    private String ctgrNm;
    
    // GOODS_NM: 물건 상세 (API 필드명: GOODS_NM)
    private String goodsNm;

    // 추가: LandItemDTO와 동일하게 상태값 및 마감일시 추가 (파싱 대비)
    private String pbctStatNm; // PBCT_CLTR_STAT_NM
    private LocalDateTime pbctClsDtm; // PBCT_CLS_DTM
}