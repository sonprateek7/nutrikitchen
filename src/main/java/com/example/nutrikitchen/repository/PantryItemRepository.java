package com.example.nutrikitchen.repository;

import com.example.nutrikitchen.domain.PantryItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {
    Optional<PantryItem> findByFoodId(UUID foodId);
    void deleteByFoodId(UUID foodId);
}
