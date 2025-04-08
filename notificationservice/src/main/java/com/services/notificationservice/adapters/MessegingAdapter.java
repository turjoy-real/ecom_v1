package com.services.notificationservice.adapters;

import java.io.File;
import java.util.List;

public interface MessegingAdapter {
    void sendMsg(String recipient, String message, String subject, List<File> attachment);
}
