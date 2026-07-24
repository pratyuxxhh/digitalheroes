package com.example.digitalocean.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UiController {

    @GetMapping("/")
    public String index(){
        return "index";
    }
    @RequestMapping("/404")
    public String pageNotFound() {
        return "404";
    }
}
