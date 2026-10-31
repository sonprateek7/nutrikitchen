package com.example.nutrikitchen.repository;

import com.example.nutrikitchen.domain.Recipe;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Recipe> findAllByOrderByNameAsc();
    void deleteByItemsFoodId(UUID foodId);
}
