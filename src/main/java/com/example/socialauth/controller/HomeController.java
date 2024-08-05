package com.example.socialauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // 기본 홈페이지
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // 로그인 페이지
    }

    @GetMapping("/success")
    public String success() {
        return "success"; // 로그인 성공 페이지
    }
}
