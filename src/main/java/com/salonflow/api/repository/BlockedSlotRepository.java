package com.salonflow.api.repository;

import com.salonflow.api.entity.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {

    List<BlockedSlot> findBySalonId(Long salonId);

    Optional<BlockedSlot> findBySalonIdAndDateTime(Long salonId, LocalDateTime dateTime);

    List<BlockedSlot> findBySalonIdAndDateTimeBetween(Long salonId, LocalDateTime start, LocalDateTime end);
}