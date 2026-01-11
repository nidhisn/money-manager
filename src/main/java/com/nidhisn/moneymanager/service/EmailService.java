package com.nidhisn.moneymanager.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private final JavaMailSender mailSender;

    @Value("${BREVO_FROM_EMAIL}")
    private String fromMail;

    @PostConstruct
    void test() {
        System.out.println("FROM = " + fromMail);
    }


    public void sendEmail(String to, String subject, String body){
        try{
            SimpleMailMessage message= new SimpleMailMessage();
            message.setFrom(fromMail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

        }catch(Exception e){
            throw  new RuntimeException(e.getMessage());
        }

    }
}
