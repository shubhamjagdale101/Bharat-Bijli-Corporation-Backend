package com.shubham.lightbill.lightbill_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendOtpMail(String email, String username, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otp", otp);
        String htmlContent = templateEngine.process("otp-email-template.html", context);

        helper.setFrom("shubhamjagdalerxl@gmail.com");
        helper.setTo(email);
        helper.setSubject("Your OTP Code");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendMailWithInvoiceAttachment(String subject, String email, File attachment, String body, String attachmentFileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("shubhamjagdalerxl@gmail.com");
        helper.setTo(email);
        helper.setSubject("Invoice Mail");
        helper.setText(body);
        if(attachment != null) helper.addAttachment(attachmentFileName, attachment);

        mailSender.send(message);
    }
}
