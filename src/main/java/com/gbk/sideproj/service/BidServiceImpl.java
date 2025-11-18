package com.gbk.sideproj.service;

import com.gbk.sideproj.domain.Bid;
import com.gbk.sideproj.mapper.BidMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidMapper bidMapper;

    @Override
    public void placeBid(
            String userId,
            String plnmNo,
            String pbctNo,
            String itemName,
            Long minBidPrice,
            Long bidPrice
    ) {
        // 서버 검증
        if (bidPrice < minBidPrice) {
            throw new IllegalArgumentException("최저입찰가 이상으로 입력해주세요.");
        }

        // Bid 객체 생성
        Bid bid = new Bid();
        bid.setUserId(userId);
        bid.setPlnmNo(plnmNo);
        bid.setPbctNo(pbctNo);
        bid.setItemName(itemName);
        bid.setMinBidPrice(minBidPrice);
        bid.setBidPrice(bidPrice);
        bid.setStatus("입찰완료");

        // DB insert
        bidMapper.insertBid(bid);
    }
}
