package com.example.socialauth.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/social")
    public String socialSuccess() {
        return "success";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
