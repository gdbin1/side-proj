package com.gbk.sideproj.service;

public interface BidService {

    void placeBid(
            String userId,
            String plnmNo,
            String pbctNo,
            String itemName,
            Long minBidPrice,
            Long bidPrice
    );
}
