package com.services.notificationservice.adapters;

import java.io.File;
import java.util.List;

public class PushNotificationAdapter implements MessegingAdapter {

    @Override
    public void sendMsg(String recipient, String message, String subject, List<File> attachment) {
        // Implementation for sending push notification
        System.out.println("Sending push notification to " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        if (attachment != null && !attachment.isEmpty()) {
            System.out.println("Attachments: " + attachment);
        }
    }
    
}
