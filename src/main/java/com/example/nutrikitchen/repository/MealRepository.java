package com.example.nutrikitchen.repository;

import com.example.nutrikitchen.domain.Meal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, UUID> {
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Meal> findAllByDateOrderByTimeAsc(LocalDate date);
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Meal> findAllByOrderByDateDescTimeAsc();
    boolean existsByItemsFoodId(UUID foodId);
}
