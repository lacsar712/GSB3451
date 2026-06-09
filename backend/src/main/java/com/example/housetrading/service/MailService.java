package com.example.housetrading.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String from;

    public void sendResetPasswordMail(String to, String resetLink) {
        if (mailSender == null) {
            System.out.println("SMTP not configured. Reset link for " + to + ": " + resetLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("找回密码 - 二手房交易系统");
            message.setText("您好，请点击以下链接重置您的密码：\n" + resetLink + "\n该链接有效时间为15分钟。");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ". Sending to console instead.");
            System.out.println("Reset link for " + to + ": " + resetLink);
        }
    }
}
