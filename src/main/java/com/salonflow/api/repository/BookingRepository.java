package com.salonflow.api.repository;

import com.salonflow.api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBySalonId(Long salonId);

    // ⚡ MASTERSTROKE FOR THESIS: Solves the memory-heavy loop issue in a single DB query
    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.client " +
            "LEFT JOIN FETCH b.service " +
            "WHERE b.salonId = :salonId")
    List<Booking> findBySalonIdEnriched(@Param("salonId") Long salonId);

    // ⚡ MASTERSTROKE FOR THESIS: Pre-enriches user dashboard lookups via DB Joins
    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.salon " +
            "LEFT JOIN FETCH b.service " +
            "WHERE LOWER(b.userContact) = LOWER(:userContact)")
    List<Booking> findByUserContactIgnoreCaseEnriched(@Param("userContact") String userContact);

    // Replaces dangerous textual "StartingWith" query pattern with high-speed indexing boundaries
    List<Booking> findBySalonIdAndBookingDateTimeBetween(Long salonId, LocalDateTime start, LocalDateTime end);

    boolean existsBySalonIdAndBookingDateTime(Long salonId, LocalDateTime bookingDateTime);

    boolean existsByUserContactIgnoreCaseAndBookingDateTime(String userContact, LocalDateTime bookingDateTime);

    List<Booking> findByUserContactIgnoreCase(String userContact);
}