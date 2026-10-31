package com.example.nutrikitchen.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    private UUID id;

    @NotBlank
    @Size(max = 70)
    @Column(nullable = false, length = 70)
    private String name;

    @Size(max = 240)
    @Column(length = 240)
    private String description;

    @DecimalMin("1")
    @DecimalMax("1440")
    @Column(name = "prep_minutes", nullable = false)
    private int prepMinutes;

    @DecimalMin("1")
    @DecimalMax("100")
    @Column(nullable = false)
    private int servings;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id")
    private List<RecipeIngredient> items = new ArrayList<>();

    protected Recipe() {
    }

    public Recipe(UUID id, String name, String description, int prepMinutes, int servings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
        this.servings = servings;
    }

    public void addItem(RecipeIngredient item) {
        items.add(item);
        item.setRecipe(this);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrepMinutes() { return prepMinutes; }
    public int getServings() { return servings; }
    public List<RecipeIngredient> getItems() { return items; }
}
