package com.salonflow.api.repository;

import com.salonflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByContactIgnoreCase(String contact);
    Optional<User> findByContactIgnoreCaseOrPhoneIgnoreCase(String contact, String phone);
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
}