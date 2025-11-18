package com.gbk.sideproj.domain;

public class DetailedItemResponse {
	
	 private String plnmNo;
	    private String pbctNo;
	    private String plnmNm;      // 공고명
	    private String ldnmAdr;     // 주소
	    private String apslAmt;     // 감정가
	    private String minBidPrc;   // 최저입찰가
	    private String dtlsPageUrl; // 상세 페이지 URL

	    private String detailUrl;

	    public String getDetailUrl() {
	        return detailUrl;
	    }

	    public void setDetailUrl(String detailUrl) {
	        this.detailUrl = detailUrl;
	    }

	    
	    public String getPlnmNo() {
	        return plnmNo;
	    }

	    public void setPlnmNo(String plnmNo) {
	        this.plnmNo = plnmNo;
	    }

	    public String getPbctNo() {
	        return pbctNo;
	    }

	    public void setPbctNo(String pbctNo) {
	        this.pbctNo = pbctNo;
	    }

	    public String getPlnmNm() {
	        return plnmNm;
	    }

	    public void setPlnmNm(String plnmNm) {
	        this.plnmNm = plnmNm;
	    }

	    public String getLdnmAdr() {
	        return ldnmAdr;
	    }

	    public void setLdnmAdr(String ldnmAdr) {
	        this.ldnmAdr = ldnmAdr;
	    }

	    public String getApslAmt() {
	        return apslAmt;
	    }

	    public void setApslAmt(String apslAmt) {
	        this.apslAmt = apslAmt;
	    }

	    public String getMinBidPrc() {
	        return minBidPrc;
	    }

	    public void setMinBidPrc(String minBidPrc) {
	        this.minBidPrc = minBidPrc;
	    }

	    public String getDtlsPageUrl() {
	        return dtlsPageUrl;
	    }

	    public void setDtlsPageUrl(String dtlsPageUrl) {
	        this.dtlsPageUrl = dtlsPageUrl;
	    }

}
