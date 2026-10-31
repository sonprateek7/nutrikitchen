package com.example.nutrikitchen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem food;

    @DecimalMin(value = "0.001")
    @DecimalMax(value = "100000")
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    protected RecipeIngredient() {
    }

    public RecipeIngredient(UUID id, FoodItem food, BigDecimal quantity) {
        this.id = id;
        this.food = food;
        this.quantity = quantity;
    }

    void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public FoodItem getFood() { return food; }
    public BigDecimal getQuantity() { return quantity; }
}
