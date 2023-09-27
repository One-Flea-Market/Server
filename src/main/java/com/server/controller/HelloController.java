package com.server.controller;

import com.server.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class HelloController {
    @GetMapping("/")
    public String Hello() {
        return "Hello";
    }
}
