package com.gbk.sideproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {
	
	@GetMapping("/")
	public String main() {
		return "main";
	}
	
	@GetMapping("/items-page")
	public String itemsPage() {
		return "items";
	}
	
	@GetMapping("/guide")
	public String guidePage() {
	    return "guide";
	}
	
//	@GetMapping("/contact") // (나중에 고객센터용도 추가 예정)
//    public String contactPage() {
//        return "contact";
//    }
}
