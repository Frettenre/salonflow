package com.salonflow.api.repository;

import com.salonflow.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Now executes true native temporal indexing sort on the database cluster level
    List<Review> findBySalonIdOrderByDateDesc(Long salonId);
}