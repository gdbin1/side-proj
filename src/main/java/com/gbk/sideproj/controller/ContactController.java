package com.gbk.sideproj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import com.gbk.sideproj.mapper.ContactMapper;
import com.gbk.sideproj.domain.Contact;
import com.gbk.sideproj.domain.User;

@Controller
public class ContactController {

    @Autowired
    private ContactMapper contactMapper;

    // 문의하기 페이지 (GET)
    @GetMapping("/contact")
    public String contactForm() {
        return "contact";
    }

    // 문의 내용 전송 (POST)
    @PostMapping("/contact")
    public String submitContact(@RequestParam String title,
                                @RequestParam String content,
                                HttpSession session,
                                Model model) {

        // 로그인된 사용자 확인
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            model.addAttribute("notLogin", true); // 로그인 안 된 상태 표시
            return "contact"; // contact.html 다시 표시 (모달로 안내)
        }

        // 문의 정보 생성
        Contact contact = new Contact();
        contact.setUserId(user.getUsername());
        contact.setName(user.getName());
        contact.setTitle(title);
        contact.setContent(content);

        // DB 저장
        contactMapper.insertContact(contact);

        model.addAttribute("success", "문의가 성공적으로 접수되었습니다!");
        return "contact";
    }
}
