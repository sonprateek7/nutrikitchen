package com.example.nutrikitchen.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class ApiModels {

    private ApiModels() {
    }

    public record FoodRequest(
            @NotBlank @Size(max = 70) String name,
            @NotBlank @Size(max = 30) String category,
            @NotBlank @Size(max = 16) String unit,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal calories,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal protein,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal carbs,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal fat,
            @NotBlank @Size(max = 30) String confidence) {
    }

    public record FoodResponse(UUID id, String name, String category, String unit, BigDecimal calories,
                               BigDecimal protein, BigDecimal carbs, BigDecimal fat, String confidence) {
    }

    public record GoalsRequest(
            @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal calories,
            @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal protein,
            @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal carbs,
            @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal fat) {
    }

    public record MealItemRequest(
            @NotNull UUID foodId,
            @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantity) {
    }

    public record MealRequest(
            @NotBlank @Size(max = 20) String type,
            @NotNull LocalDate date,
            @NotNull LocalTime time,
            @Size(max = 160) String notes,
            @NotNull @Size(min = 1, max = 100) List<@Valid MealItemRequest> items) {
    }

    public record MealItemResponse(UUID foodId, String name, String unit, BigDecimal quantity,
                                   BigDecimal calories, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
    }

    public record MealResponse(UUID id, String type, LocalDate date, LocalTime time, String notes,
                               List<MealItemResponse> items, NutritionTotals totals) {
    }

    public record NutritionTotals(BigDecimal calories, BigDecimal protein, BigDecimal carbs, BigDecimal fat) {
        public static NutritionTotals zero() {
            return new NutritionTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public NutritionTotals plus(NutritionTotals other) {
            return new NutritionTotals(calories.add(other.calories), protein.add(other.protein),
                    carbs.add(other.carbs), fat.add(other.fat));
        }
    }

    public record DailySummary(LocalDate date, NutritionTotals totals, GoalsRequest goals,
                               NutritionTotals remaining, List<MealResponse> meals) {
    }

    public record PantryRequest(
            @NotNull UUID foodId,
            @NotNull @DecimalMin("0") @DecimalMax("100000") BigDecimal quantity,
            @NotNull @DecimalMin("0") @DecimalMax("100000") BigDecimal lowAt,
            LocalDate expires) {
    }

    public record PantryAdjustment(
            @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantity,
            @NotNull Boolean add) {
    }

    public record PantryResponse(UUID id, UUID foodId, String name, String category, String unit,
                                 BigDecimal quantity, BigDecimal lowAt, LocalDate expires, boolean lowStock) {
    }

    public record RecipeIngredientRequest(
            @NotNull UUID foodId,
            @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantity) {
    }

    public record RecipeRequest(
            @NotBlank @Size(max = 70) String name,
            @Size(max = 240) String description,
            @NotNull @DecimalMin("1") @DecimalMax("1440") Integer prepMinutes,
            @NotNull @DecimalMin("1") @DecimalMax("100") Integer servings,
            @NotNull @Size(min = 1, max = 100) List<@Valid RecipeIngredientRequest> items) {
    }

    public record RecipeIngredientResponse(UUID foodId, String name, String unit, BigDecimal quantity) {
    }

    public record RecipeResponse(UUID id, String name, String description, int prepMinutes, int servings,
                                 List<RecipeIngredientResponse> items, NutritionTotals nutritionPerServing,
                                 boolean ingredientsAvailable) {
    }

    public record RecommendationResponse(RecipeResponse recipe, List<String> reasons) {
    }

    public record KitchenImportRequest(
            @NotNull @Valid GoalsRequest goals,
            @NotNull @Size(max = 10000) List<@Valid ImportedFood> foods,
            @NotNull @Size(max = 10000) List<@Valid ImportedMeal> meals,
            @NotNull @Size(max = 10000) List<@Valid ImportedPantryItem> pantry,
            @NotNull @Size(max = 10000) List<@Valid ImportedRecipe> recipes) {
    }

    public record ImportedFood(
            @NotBlank String id,
            @NotBlank @Size(max = 70) String name,
            @NotBlank @Size(max = 30) String category,
            @NotBlank @Size(max = 16) String unit,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal calories,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal protein,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal carbs,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal fat,
            @NotBlank @Size(max = 30) String confidence) {
    }

    public record ImportedMeal(
            @NotBlank String id,
            @NotBlank @Size(max = 20) String type,
            @NotNull LocalDate date,
            @NotNull LocalTime time,
            @Size(max = 160) String notes,
            @NotNull @Size(min = 1, max = 100) List<@Valid ImportedMealItem> items) {
    }

    public record ImportedMealItem(
            @NotBlank String foodId,
            @NotBlank @Size(max = 70) String name,
            @NotBlank @Size(max = 16) String unit,
            @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantity,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal calories,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal protein,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal carbs,
            @NotNull @DecimalMin("0") @DecimalMax("10000") BigDecimal fat) {
    }

    public record ImportedPantryItem(
            @NotBlank String id,
            @NotBlank String foodId,
            @NotNull @DecimalMin("0") @DecimalMax("100000") BigDecimal quantity,
            @NotNull @DecimalMin("0") @DecimalMax("100000") BigDecimal lowAt,
            LocalDate expires) {
    }

    public record ImportedRecipe(
            @NotBlank String id,
            @NotBlank @Size(max = 70) String name,
            @Size(max = 240) String description,
            @NotNull @DecimalMin("1") @DecimalMax("1440") Integer prepMinutes,
            @NotNull @DecimalMin("1") @DecimalMax("100") Integer servings,
            @NotNull @Size(min = 1, max = 100) List<@Valid ImportedRecipeIngredient> items) {
    }

    public record ImportedRecipeIngredient(
            @NotBlank String foodId,
            @NotNull @DecimalMin("0.001") @DecimalMax("100000") BigDecimal quantity) {
    }

    public record ApiError(String code, String message, java.time.Instant timestamp, List<String> details) {
    }
}
