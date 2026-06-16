package com.salonflow.api.repository;

import com.salonflow.api.entity.Category;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Cacheable(value = "categories") // <-- Cache all categories in memory
    List<Category> findAll();
}