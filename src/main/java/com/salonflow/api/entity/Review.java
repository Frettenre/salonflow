package com.salonflow.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        // Fixed: columnList must point to the physical DB column "salon_id", not the Java property "salonId"
        @Index(name = "idx_review_salon_id", columnList = "salon_id"),
        @Index(name = "idx_review_date", columnList = "review_date") // Added for rapid descending sort scans
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stars;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime date; // Optimized: Converted from String to LocalDateTime

    // =========================================================================
    // RELATIONAL MAPPINGS (Brings professional integrity to your thesis design)
    // =========================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", insertable = false, updatable = false)
    private Salon salon;

    // =========================================================================
    // GETTERS AND SETTERS
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }
}