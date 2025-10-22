package org.example.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件中读取发件人邮箱
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // ✅ 设置发件人（必须和授权邮箱一致）
        message.setTo(toEmail);
        message.setSubject("【博客系统】注册验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效，请勿泄露。");

        mailSender.send(message);
        System.out.println("✅ 验证码 " + code + " 已发送到邮箱：" + toEmail);
    }
}

