package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "salons", indexes = {
        @Index(name = "idx_salon_owner_contact", columnList = "owner_contact")
})
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ro", nullable = false, columnDefinition = "TEXT")
    private String descriptionRo;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "is_recommended", nullable = false)
    private boolean isRecommended = false;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "owner_contact")
    private String ownerContact;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionRo() { return descriptionRo; }
    public void setDescriptionRo(String descriptionRo) { this.descriptionRo = descriptionRo; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public String getOwnerContact() { return ownerContact; }
    public void setOwnerContact(String ownerContact) { this.ownerContact = ownerContact; }
}