package com.salonflow.api.repository;

import com.salonflow.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Spring Data automatically handles sorted database retrieval
    List<Review> findBySalonIdOrderByDateDesc(Long salonId);
}