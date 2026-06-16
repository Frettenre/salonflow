package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_salon_id", columnList = "salonId")
})
public class Review {
    // Your existing entity fields and relationships...

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

    @Column(nullable = false)
    private String date;

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
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}