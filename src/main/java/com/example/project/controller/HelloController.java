package com.example.project.controller;

import com.example.project.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    // HelloService 의존성 주입
    private final HelloService helloService;

    @Autowired
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("data", "Hello World");
        return "hello";
    }

    @GetMapping("/hello-service")  // 경로 변경
    public String helloFromService(Model model) {
        String welcomeMessage = helloService.getWelcomeMessage();
        model.addAttribute("data", welcomeMessage);
        return "hello";
    }
}