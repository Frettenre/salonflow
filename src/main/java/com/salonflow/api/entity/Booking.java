package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user_contact", columnList = "user_contact"),
        @Index(name = "idx_booking_salon_id", columnList = "salon_id")
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "booking_date_time", nullable = false)
    private String bookingDateTime;

    @Column(name = "user_contact", nullable = false)
    private String userContact;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(String bookingDateTime) { this.bookingDateTime = bookingDateTime; }
    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }
}