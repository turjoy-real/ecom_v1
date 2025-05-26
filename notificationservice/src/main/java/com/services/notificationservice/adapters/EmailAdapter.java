package com.services.notificationservice.adapters;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class EmailAdapter implements MessegingAdapter {

    @Autowired
    private JavaMailSender javaMailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailAdapter.class);

    @Override
    public void sendMsg(String recipient, String message, String subject, List<File> attachment) {
        // Implementation for sending email
        System.out.println("Sending email to " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        // if (attachment != null && !attachment.isEmpty()) {
        // System.out.println("Attachments: " + attachment);
        // }
        {

            // Try block to check for exceptions
            try {

                // Creating a simple mail message
                SimpleMailMessage mailMessage = new SimpleMailMessage();

                // Setting up necessary details
                mailMessage.setFrom("dev@turjoysaha.com");
                mailMessage.setTo(recipient);
                mailMessage.setText(message);
                mailMessage.setSubject(subject);

                // Sending the mail
                javaMailSender.send(mailMessage);
                logger.info("Mail Sent Successfully...");

            }

            // Catch block to handle the exceptions
            catch (Exception e) {
                logger.error("Mail sending failed to {}. Reason: {}", recipient, e.getMessage(), e);
            }
        }

    }

}
