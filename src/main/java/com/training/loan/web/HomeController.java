package com.training.loan.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "index";
        }
        boolean officer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER")
                        || a.getAuthority().equals("ROLE_ADMIN"));
        return officer ? "redirect:/officer/queue" : "redirect:/applications";
    }
}
