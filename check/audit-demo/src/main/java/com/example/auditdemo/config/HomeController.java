package com.example.auditdemo.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller for navigation
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/audit/pending";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:/audit/pending";
    }
}
