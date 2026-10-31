package com.example.nutrikitchen.api;

import static com.example.nutrikitchen.api.ApiModels.*;

import com.example.nutrikitchen.service.KitchenService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KitchenController {

    private final KitchenService kitchen;

    public KitchenController(KitchenService kitchen) {
        this.kitchen = kitchen;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "nutrikitchen");
    }

    @PostMapping("/data/import")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void importKitchenData(@Valid @RequestBody KitchenImportRequest request) {
        kitchen.importKitchenData(request);
    }

    @GetMapping("/goals")
    public GoalsRequest getGoals() {
        return kitchen.getGoals();
    }

    @PutMapping("/goals")
    public GoalsRequest updateGoals(@Valid @RequestBody GoalsRequest request) {
        return kitchen.updateGoals(request);
    }

    @GetMapping("/foods")
    public List<FoodResponse> foods() {
        return kitchen.listFoods();
    }

    @PostMapping("/foods")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse createFood(@Valid @RequestBody FoodRequest request) {
        return kitchen.createFood(request);
    }

    @PutMapping("/foods/{id}")
    public FoodResponse updateFood(@PathVariable UUID id, @Valid @RequestBody FoodRequest request) {
        return kitchen.updateFood(id, request);
    }

    @DeleteMapping("/foods/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable UUID id) {
        kitchen.deleteFood(id);
    }

    @GetMapping("/meals")
    public List<MealResponse> meals(@RequestParam(required = false) LocalDate date) {
        return kitchen.listMeals(date);
    }

    @PostMapping("/meals")
    @ResponseStatus(HttpStatus.CREATED)
    public MealResponse createMeal(@Valid @RequestBody MealRequest request) {
        return kitchen.createMeal(request);
    }

    @DeleteMapping("/meals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMeal(@PathVariable UUID id) {
        kitchen.deleteMeal(id);
    }

    @GetMapping("/daily-summary")
    public DailySummary dailySummary(@RequestParam(required = false) LocalDate date) {
        return kitchen.dailySummary(date == null ? LocalDate.now() : date);
    }

    @GetMapping("/pantry")
    public List<PantryResponse> pantry() {
        return kitchen.listPantry();
    }

    @PostMapping("/pantry")
    @ResponseStatus(HttpStatus.CREATED)
    public PantryResponse createPantryItem(@Valid @RequestBody PantryRequest request) {
        return kitchen.createPantryItem(request);
    }

    @PutMapping("/pantry/{id}")
    public PantryResponse updatePantryItem(@PathVariable UUID id, @Valid @RequestBody PantryRequest request) {
        return kitchen.updatePantryItem(id, request);
    }

    @PatchMapping("/pantry/{id}/adjust")
    public PantryResponse adjustPantry(@PathVariable UUID id, @Valid @RequestBody PantryAdjustment request) {
        return kitchen.adjustPantry(id, request);
    }

    @DeleteMapping("/pantry/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePantryItem(@PathVariable UUID id) {
        kitchen.deletePantryItem(id);
    }

    @GetMapping("/recipes")
    public List<RecipeResponse> recipes() {
        return kitchen.listRecipes();
    }

    @PostMapping("/recipes")
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse createRecipe(@Valid @RequestBody RecipeRequest request) {
        return kitchen.createRecipe(request);
    }

    @DeleteMapping("/recipes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable UUID id) {
        kitchen.deleteRecipe(id);
    }

    @GetMapping("/recommendations")
    public List<RecommendationResponse> recommendations(@RequestParam(required = false) LocalDate date) {
        return kitchen.recommendations(date == null ? LocalDate.now() : date);
    }

    public record HealthResponse(String status, String application) {
    }
}
