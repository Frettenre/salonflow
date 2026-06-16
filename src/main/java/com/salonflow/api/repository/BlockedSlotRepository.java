package com.salonflow.api.repository;

import com.salonflow.api.entity.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {
    List<BlockedSlot> findBySalonId(Long salonId);
    Optional<BlockedSlot> findBySalonIdAndDateTime(Long salonId, String dateTime);

    // ADDED: This enables prefix matching (e.g., matching "2023-10-27" in ISO strings like "2023-10-27T10:00")
    List<BlockedSlot> findBySalonIdAndDateTimeStartingWith(Long salonId, String datePrefix);
}