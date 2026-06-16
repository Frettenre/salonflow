package com.salonflow.api.repository;

public interface SalonSummaryDto {
    Long getId();
    String getName();
    Double getRating();
    String getImageUrl();
    String getDescriptionEn();
    String getDescriptionRo();
}