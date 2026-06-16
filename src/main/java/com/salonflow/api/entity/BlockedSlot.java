package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "blocked_slots", indexes = {
        @Index(name = "idx_blocked_slot_salon_id", columnList = "salon_id")
})
public class BlockedSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "date_time", nullable = false)
    private String dateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
}