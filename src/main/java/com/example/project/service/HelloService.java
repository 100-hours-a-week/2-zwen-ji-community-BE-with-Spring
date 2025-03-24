package com.example.project.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    public String getWelcomeMessage() {
        return "서비스 계층에서 온 환영 메시지!";
    }
}
