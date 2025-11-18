package com.gbk.sideproj.mapper;

import java.util.List;

import com.gbk.sideproj.domain.Bid;

public interface BidMapper {
	
	void insertBid(Bid bid);
	
	List<Bid> findByUserId(String userId);
}
