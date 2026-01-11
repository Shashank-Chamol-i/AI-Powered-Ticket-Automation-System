package com.example.TicketAutomation.service;

import com.example.TicketAutomation.Repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final Executor emailExecutor;


    public void sendEmail(String email, String subject, String body) {
        emailExecutor.execute(()->{

            try{
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message,true);
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(body,true);
                helper.setFrom(new InternetAddress("shashankchamoli069@gmail.com","Ticket-Automation"));
                mailSender.send(message);

            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        });
    }

}
