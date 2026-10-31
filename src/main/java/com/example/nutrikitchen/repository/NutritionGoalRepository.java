package com.example.nutrikitchen.repository;

import com.example.nutrikitchen.domain.NutritionGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, Long> {
}
