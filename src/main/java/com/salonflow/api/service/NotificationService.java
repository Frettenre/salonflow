package com.salonflow.api.service;

import com.salonflow.api.entity.Notification;
import com.salonflow.api.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired private NotificationRepository notificationRepository;

    public void createNotification(String userContact, String titleEn, String titleRo, String messageEn, String messageRo) {
        try {
            Notification notification = new Notification();
            notification.setUserContact(userContact.trim().toLowerCase());
            notification.setTitleEn(titleEn);
            notification.setTitleRo(titleRo);
            notification.setMessageEn(messageEn);
            notification.setMessageRo(messageRo);
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Failed to create notification: " + e.getMessage());
        }
    }
}