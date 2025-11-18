package com.gbk.sideproj.domain;

import lombok.Data;

@Data
public class WebItemDetail {
    private Long id;
    private String plnmNo;
    private String pbctNo;

    private Integer rnum;
    private String pbctCdtnNo;
    private String cltrNo;
    private String cltrHstrNo;
    private String scrnGrpCd;
    private String ctgrFullNm;
    private String bidMnmtNo;
    private String cltrNm;
    private String cltrMnmtNo;
    private String ldnmAdrs;
    private String nmrdAdrs;
    private String ldnmPnu;
    private String dpslMtdCd;
    private String dpslMtdNm;
    private String bidMtdNm;
    private Long minBidPrc;
    private Long apslAsesAvgAmt;
    private String feeRate;
    private String pbctBegnDtm;
    private String pbctClsDtm;
    private String pbctCltrStatNm;
    private Integer uscbCnt;
    private Integer iqryCnt;
    private String goodsNm;
    private String cltrImgFiles;
}
