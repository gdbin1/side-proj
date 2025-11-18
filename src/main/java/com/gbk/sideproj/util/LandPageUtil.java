package com.gbk.sideproj.util;

import lombok.Getter;

@Getter
public class LandPageUtil {
	
	private int currentPage; 	
	private int itemsPerPage; 	
	private int pagesPerBlock; 	
	private int totalItems; 	
	
	private int totalPages; 	
	private int startPage; 		
	private int endPage; 		
	private boolean hasPrev; 	
	private boolean hasNext; 	

	public LandPageUtil(int currentPage, int itemsPerPage, int pagesPerBlock, int totalItems) {
		this.currentPage = currentPage;
		this.itemsPerPage = itemsPerPage;
		this.pagesPerBlock = pagesPerBlock;
		this.totalItems = totalItems;
		
		calculatePageInfo(); 
	}
	
	private void calculatePageInfo() {
		this.totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
		
		this.endPage = (int) (Math.ceil((double) currentPage / pagesPerBlock) * pagesPerBlock);
		
		this.startPage = this.endPage - pagesPerBlock + 1;
		
		if (this.endPage > totalPages) {
			this.endPage = totalPages;
		}
		
		this.hasPrev = this.startPage > 1; 
		this.hasNext = this.endPage < totalPages;
		
		if (this.currentPage > totalPages && totalPages > 0) {
			this.currentPage = totalPages;
		} else if (totalPages == 0) {
            this.currentPage = 1;
        }
	}
}