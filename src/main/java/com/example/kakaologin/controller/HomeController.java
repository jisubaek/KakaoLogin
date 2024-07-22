package com.example.kakaologin.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            Map<String, Object> attributes = principal.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String username = (String) profile.get("nickname");
            String email = (String) kakaoAccount.get("email");
            String profileImageUrl = (String) profile.get("profile_image_url");

            model.addAttribute("name", username);
            model.addAttribute("email", email);
            model.addAttribute("profileImageUrl", profileImageUrl);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
