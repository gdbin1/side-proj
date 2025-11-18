package com.gbk.sideproj.domain;

import lombok.Data;

@Data
public class Bid {
		
	private Integer id;
    private String userId;
    private String plnmNo;
    private String pbctNo;
    private String itemName;
    private Long minBidPrice;
    private Long bidPrice;
    private String bidTime;
    private String status;

}
