package com.codepresso.codepresso.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 인증 관련 뷰 컨트롤러
 */
@Controller
@RequestMapping("/auth")
public class AuthViewController {

    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/password-find")
    public String passwordFindPage() {
        return "auth/password-find";
    }

    @GetMapping("/id-find")
    public String idFindPage() {
        return "auth/id-find";
    }
}
