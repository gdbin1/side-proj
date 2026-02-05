package com.gbk.sideproj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gbk.sideproj.domain.User;
import com.gbk.sideproj.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(User user, Model model) {
        userService.register(user);
        model.addAttribute("msg", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "login";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {

        User user = userService.login(username, password);

        if (user != null) {
            // ✅ 로그인 성공 시 세션에 사용자 저장
            session.setAttribute("loginUser", user);

            // ✅ 로그인 후 이전 페이지로 돌아가거나, 기본적으로 메인으로 이동
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }

            return "redirect:/"; // 기본적으로 메인 페이지로 이동
        } else {
            // 로그인 실패
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "login";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
