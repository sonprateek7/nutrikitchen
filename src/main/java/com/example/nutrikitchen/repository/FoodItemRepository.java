package com.example.nutrikitchen.repository;

import com.example.nutrikitchen.domain.FoodItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
