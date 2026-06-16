package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services", indexes = {
        @Index(name = "idx_service_salon_id", columnList = "salon_id")
})
public class SalonService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ro", nullable = false)
    private String nameRo;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameRo() { return nameRo; }
    public void setNameRo(String nameRo) { this.nameRo = nameRo; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Long getSalonId() { return salonId; }
    public void setSalonId(Long salonId) { this.salonId = salonId; }
}