package com.salonflow.api.repository;

import com.salonflow.api.entity.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRepository extends JpaRepository<SalonService, Long> {
    List<SalonService> findBySalonId(Long salonId);
}