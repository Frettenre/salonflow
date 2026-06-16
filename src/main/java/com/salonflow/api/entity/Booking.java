package com.salonflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime bookingDateTime; // Optimized: Converted from String to LocalDateTime

    @Column(name = "user_contact", nullable = false)
    private String userContact;

    // =========================================================================
    // RELATIONAL MAPPINGS (Optimized for single-query JOIN FETCH execution)
    // =========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", insertable = false, updatable = false)
    private Salon salon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private SalonService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_contact", referencedColumnName = "contact", insertable = false, updatable = false)
    private User client;

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(LocalDateTime bookingDateTime) { this.bookingDateTime = bookingDateTime; }

    public String getUserContact() { return userContact; }
    public void setUserContact(String userContact) { this.userContact = userContact; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public SalonService getService() { return service; }
    public void setService(SalonService service) { this.service = service; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
}