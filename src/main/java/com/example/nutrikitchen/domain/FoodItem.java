package com.example.nutrikitchen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    private UUID id;

    @NotBlank
    @Size(max = 70)
    @Column(nullable = false, unique = true, length = 70)
    private String name;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String category;

    @NotBlank
    @Size(max = 16)
    @Column(name = "serving_unit", nullable = false, length = 16)
    private String unit;

    @DecimalMin("0")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal calories;

    @DecimalMin("0")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal protein;

    @DecimalMin("0")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal carbs;

    @DecimalMin("0")
    @DecimalMax("10000")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal fat;

    @NotBlank
    @Column(name = "source_confidence", nullable = false, length = 30)
    private String confidence;

    protected FoodItem() {
    }

    public FoodItem(UUID id, String name, String category, String unit, BigDecimal calories,
                    BigDecimal protein, BigDecimal carbs, BigDecimal fat, String confidence) {
        this.id = id;
        update(name, category, unit, calories, protein, carbs, fat, confidence);
    }

    public void update(String name, String category, String unit, BigDecimal calories,
                       BigDecimal protein, BigDecimal carbs, BigDecimal fat, String confidence) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.confidence = confidence;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public BigDecimal getCalories() { return calories; }
    public BigDecimal getProtein() { return protein; }
    public BigDecimal getCarbs() { return carbs; }
    public BigDecimal getFat() { return fat; }
    public String getConfidence() { return confidence; }
}
