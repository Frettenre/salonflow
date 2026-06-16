package com.salonflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_contact", nullable = false)
    private String userContact;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "title_ro", nullable = false)
    private String titleRo;

    @Column(name = "message_en", nullable = false, columnDefinition = "TEXT")
    private String messageEn;

    @Column(name = "message_ro", nullable = false, columnDefinition = "TEXT")
    private String messageRo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }
    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public String getTitleRo() { return titleRo; }
    public void setTitleRo(String titleRo) { this.titleRo = titleRo; }
    public String getMessageEn() { return messageEn; }
    public void setMessageEn(String messageEn) { this.messageEn = messageEn; }
    public String getMessageRo() { return messageRo; }
    public void setMessageRo(String messageRo) { this.messageRo = messageRo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}