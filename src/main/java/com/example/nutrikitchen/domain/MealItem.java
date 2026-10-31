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
@Table(name = "meal_items")
public class MealItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem food;

    @Column(name = "food_name", nullable = false, length = 70)
    private String foodName;

    @Column(name = "serving_unit", nullable = false, length = 16)
    private String unit;

    @DecimalMin(value = "0.001")
    @DecimalMax(value = "100000")
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal calories;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal protein;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal carbs;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal fat;

    protected MealItem() {
    }

    public MealItem(UUID id, FoodItem food, BigDecimal quantity) {
        this(id, food, food.getName(), food.getUnit(), quantity, food.getCalories(), food.getProtein(),
                food.getCarbs(), food.getFat());
    }

    public MealItem(UUID id, FoodItem food, String foodName, String unit, BigDecimal quantity,
                    BigDecimal calories, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
        this.id = id;
        this.food = food;
        this.foodName = foodName;
        this.unit = unit;
        this.quantity = quantity;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    void setMeal(Meal meal) { this.meal = meal; }
    public UUID getId() { return id; }
    public FoodItem getFood() { return food; }
    public String getFoodName() { return foodName; }
    public String getUnit() { return unit; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getCalories() { return calories; }
    public BigDecimal getProtein() { return protein; }
    public BigDecimal getCarbs() { return carbs; }
    public BigDecimal getFat() { return fat; }
}
