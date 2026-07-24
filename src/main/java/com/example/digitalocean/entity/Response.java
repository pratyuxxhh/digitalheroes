package com.example.digitalocean.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private int status;
    private long responseTime;
    private String metaDescription;
    private String pageTitle;
    private int h1Count;
    private int missingAltImages;
    private int wordCount;
}

