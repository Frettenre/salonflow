package com.salonflow.api.repository;

import com.salonflow.api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ADDED: This resolves the compilation error by allowing lookups by Salon ID
    List<Booking> findBySalonId(Long salonId);

    List<Booking> findBySalonIdAndBookingDateTimeStartingWith(Long salonId, String datePrefix);

    boolean existsBySalonIdAndBookingDateTime(Long salonId, String bookingDateTime);

    boolean existsByUserContactIgnoreCaseAndBookingDateTime(String userContact, String bookingDateTime);

    List<Booking> findByUserContactIgnoreCase(String userContact);
}