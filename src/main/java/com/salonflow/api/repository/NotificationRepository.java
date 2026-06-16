package com.salonflow.api.repository;

import com.salonflow.api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserContactIgnoreCaseOrderByCreatedAtDesc(String userContact);
}