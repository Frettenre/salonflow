package com.salonflow.api.repository;

import com.salonflow.api.entity.Salon;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SalonRepository extends JpaRepository<Salon, Long> {

    // Heavy entity query (Keep this if you still need full Salon objects anywhere)
    List<Salon> findByCategoryId(Long categoryId);

    // ⚡ MEMORY TUNED PROJECTION: Fetches only the 5 grid-required columns for a category
    List<SalonSummaryDto> findProjectedByCategoryId(Long categoryId);

    // ⚡ MEMORY TUNED PROJECTION: Fetches lightweight summaries for the Home feed recommended grid
    List<SalonSummaryDto> findProjectedBy();

    List<Salon> findByOwnerContactIgnoreCase(String ownerContact);
    List<SalonSummaryDto> findTop5ProjectedByOrderByRatingDesc();
    long countByOwnerContactIgnoreCase(String ownerContact);

    @Cacheable(value = "salons", key = "#id")
    Optional<Salon> findById(Long id);
}