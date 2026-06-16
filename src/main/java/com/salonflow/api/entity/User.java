package com.salonflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String contact;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String phone;
    private String birthday;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_partner_approved", nullable = false)
    private boolean isPartnerApproved = false;

    @Column(name = "applied_for_partner", nullable = false)
    private boolean appliedForPartner = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isPartnerApproved() { return isPartnerApproved; }
    public void setPartnerApproved(boolean partnerApproved) { isPartnerApproved = partnerApproved; }
    public boolean isAppliedForPartner() { return appliedForPartner; }
    public void setAppliedForPartner(boolean appliedForPartner) { this.appliedForPartner = appliedForPartner; }
}