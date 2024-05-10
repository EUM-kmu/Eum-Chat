package com.eum.haetsal.chat.domain.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class HealthCheckController {
    @GetMapping("/health")
    public String healthCheck() {

        return "OK";
    }
}
