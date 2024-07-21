package com.example.kakaologin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("nickname"));
            model.addAttribute("email", principal.getAttribute("email"));
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
