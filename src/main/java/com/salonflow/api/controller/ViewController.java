package com.salonflow.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        // Points to src/main/resources/templates/index.html
        // Thymeleaf will process this, pull in your fragments, and compile it!
        return "index";
    }
}