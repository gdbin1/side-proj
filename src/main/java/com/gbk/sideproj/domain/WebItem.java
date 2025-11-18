package com.gbk.sideproj.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class WebItem {

    private Long id;
    private String plnmNo;       // 공고번호
    private String pbctNo;       // 회차
    private String cltrNm;       // 물건명
    private String ldnAdrs;      // 주소
    private Long apslAsesAmt;    // 감정가
    private Long minBidPrc;      // 최저입찰가
    private LocalDateTime pbctBegnDtm; // 입찰 시작
    private LocalDateTime pbctClsDtm;  // 입찰 종료
    private String pbctStatNm;   // 진행상태
    private String imgUrl;       // 썸네일
    private String onbdUrl;

    // 상세 조회용 필드
    private String landUsage;  
    private String landArea;
    private String ownerType;
    private String detailDesc;
}
