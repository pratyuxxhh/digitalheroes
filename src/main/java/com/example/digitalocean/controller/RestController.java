package com.example.digitalocean.controller;


import com.example.digitalocean.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;


@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Autowired
    private UrlService urlService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }

    @PostMapping("/audit")
    public ResponseEntity<?> getTheResponse(@RequestParam String url) throws IOException {
        return urlService.audit(url);

    }
}
