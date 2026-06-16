package com.salonflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_slots", indexes = {
        @Index(name = "idx_blocked_slot_salon_id", columnList = "salon_id"),
        @Index(name = "idx_blocked_slot_date_time", columnList = "date_time") // Added index for fast chronological scans
})
public class BlockedSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime; // Optimized: Converted from String to LocalDateTime

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}