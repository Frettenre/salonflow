package com.salonflow.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ro", nullable = false)
    private String nameRo;

    @Column(name = "icon_class", nullable = false)
    private String iconClass;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameRo() { return nameRo; }
    public void setNameRo(String nameRo) { this.nameRo = nameRo; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
}