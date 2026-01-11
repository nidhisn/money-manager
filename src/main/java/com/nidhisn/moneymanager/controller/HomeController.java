package com.nidhisn.moneymanager.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/status","/health"})
public class HomeController {

    @GetMapping
    public String healthCheck(){
        return "Application is running";
    }

    @Value("${BREVO_FROM_EMAIL}")
    private String test;



    @PostConstruct
    public void check() {
        System.out.println("FROM = " + test);
    }

}
