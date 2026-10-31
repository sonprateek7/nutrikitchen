package com.example.nutrikitchen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Entity
@Table(name = "nutrition_goals")
public class NutritionGoal {

    @Id
    private Long id;

    @DecimalMin("1")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @DecimalMin("1")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal protein;

    @DecimalMin("1")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal carbs;

    @DecimalMin("1")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fat;

    protected NutritionGoal() {
    }

    public NutritionGoal(Long id, BigDecimal calories, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
        this.id = id;
        update(calories, protein, carbs, fat);
    }

    public void update(BigDecimal calories, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public Long getId() { return id; }
    public BigDecimal getCalories() { return calories; }
    public BigDecimal getProtein() { return protein; }
    public BigDecimal getCarbs() { return carbs; }
    public BigDecimal getFat() { return fat; }
}
