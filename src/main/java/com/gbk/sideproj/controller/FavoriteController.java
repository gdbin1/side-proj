package com.gbk.sideproj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.gbk.sideproj.service.FavoriteService;
import com.gbk.sideproj.domain.User; 

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model; 

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 찜 추가
     */
    @PostMapping("/add")
    @ResponseBody
    public String addFavorite(@RequestParam String plnmNo,
                              @RequestParam String pbctNo,
                              HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "로그인 후 이용해주세요.";
        }

        boolean success = favoriteService.addFavorite(user.getId(), plnmNo, pbctNo);
        return success ? "찜 목록에 추가되었습니다." : "이미 찜한 물건입니다.";
    }

    /**
     * 찜 목록 조회
     */
    @GetMapping("/list")
    public String listFavorites(HttpSession session, Model model) { // ✅ import 수정
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("favorites", favoriteService.getUserFavorites(user.getId()));
        return "favoriteList"; // templates/favorite-list.html
    }

    /**
     * 찜 삭제
     */
    @PostMapping("/remove")
    @ResponseBody
    public String removeFavorite(@RequestParam String plnmNo,
                                 @RequestParam String pbctNo,
                                 HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "로그인 후 이용해주세요.";
        }

        favoriteService.removeFavorite(user.getId(), plnmNo, pbctNo);
        return "찜 목록에서 제거되었습니다.";
    }
}
