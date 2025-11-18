package com.gbk.sideproj.controller;

import com.gbk.sideproj.domain.User;
import com.gbk.sideproj.service.BidService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    // 입찰 페이지 이동 (GET)
    @GetMapping("/bid")
    public String bidPage(
            @RequestParam String plnmNo,
            @RequestParam String pbctNo,
            @RequestParam String itemName,
            @RequestParam Long minBidPrice,
            HttpSession session,
            org.springframework.ui.Model model) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("userId", loginUser.getId());
        model.addAttribute("plnmNo", plnmNo);
        model.addAttribute("pbctNo", pbctNo);
        model.addAttribute("itemName", itemName);
        model.addAttribute("minBidPrice", minBidPrice);

        return "bid";  
    }

    // 입찰 처리 (POST)
    @PostMapping("/bid")
    @ResponseBody
    public String submitBid(
            @RequestParam String plnmNo,
            @RequestParam String pbctNo,
            @RequestParam String itemName,
            @RequestParam Long minBidPrice,
            @RequestParam Long bidPrice,
            HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "NOT_LOGIN";
        }

        try {
        	bidService.placeBid(
        		    String.valueOf(loginUser.getId()),
        		    plnmNo,
        		    pbctNo,
        		    itemName,
        		    minBidPrice,
        		    bidPrice
        		);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAIL: " + e.getMessage();
        }
    }
}
