package com.example.nutrikitchen.service;

import static com.example.nutrikitchen.api.ApiModels.*;

import com.example.nutrikitchen.domain.FoodItem;
import com.example.nutrikitchen.domain.Meal;
import com.example.nutrikitchen.domain.MealItem;
import com.example.nutrikitchen.domain.NutritionGoal;
import com.example.nutrikitchen.domain.PantryItem;
import com.example.nutrikitchen.domain.Recipe;
import com.example.nutrikitchen.domain.RecipeIngredient;
import com.example.nutrikitchen.repository.FoodItemRepository;
import com.example.nutrikitchen.repository.MealRepository;
import com.example.nutrikitchen.repository.NutritionGoalRepository;
import com.example.nutrikitchen.repository.PantryItemRepository;
import com.example.nutrikitchen.repository.RecipeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitchenService {

    private static final long GOAL_ID = 1L;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final FoodItemRepository foods;
    private final NutritionGoalRepository goals;
    private final MealRepository meals;
    private final PantryItemRepository pantry;
    private final RecipeRepository recipes;

    public KitchenService(FoodItemRepository foods, NutritionGoalRepository goals, MealRepository meals,
                         PantryItemRepository pantry, RecipeRepository recipes) {
        this.foods = foods;
        this.goals = goals;
        this.meals = meals;
        this.pantry = pantry;
        this.recipes = recipes;
    }

    @Transactional(readOnly = true)
    public List<FoodResponse> listFoods() {
        return foods.findAll().stream()
                .sorted(Comparator.comparing(FoodItem::getName, String.CASE_INSENSITIVE_ORDER))
                .map(KitchenService::foodResponse).toList();
    }

    @Transactional
    public FoodResponse createFood(FoodRequest request) {
        ensureUniqueFoodName(request.name(), null);
        FoodItem food = new FoodItem(UUID.randomUUID(), request.name().trim(), request.category().trim(),
                request.unit().trim(), request.calories(), request.protein(), request.carbs(), request.fat(),
                request.confidence().trim());
        return foodResponse(foods.save(food));
    }

    @Transactional
    public FoodResponse updateFood(UUID id, FoodRequest request) {
        FoodItem food = foodOrNotFound(id);
        ensureUniqueFoodName(request.name(), id);
        food.update(request.name().trim(), request.category().trim(), request.unit().trim(),
                request.calories(), request.protein(), request.carbs(), request.fat(), request.confidence().trim());
        return foodResponse(food);
    }

    @Transactional
    public void deleteFood(UUID id) {
        FoodItem food = foodOrNotFound(id);
        if (meals.existsByItemsFoodId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "food_in_meal_history",
                    "This food is part of meal history and cannot be deleted.");
        }
        pantry.deleteByFoodId(id);
        List<Recipe> affectedRecipes = recipes.findAllByOrderByNameAsc().stream()
                .filter(recipe -> recipe.getItems().stream().anyMatch(item -> item.getFood().getId().equals(id)))
                .toList();
        recipes.deleteAll(affectedRecipes);
        foods.delete(food);
    }

    @Transactional
    public GoalsRequest getGoals() {
        NutritionGoal goal = goals.findById(GOAL_ID)
                .orElseGet(() -> goals.save(new NutritionGoal(GOAL_ID,
                        new BigDecimal("2000"), new BigDecimal("120"),
                        new BigDecimal("220"), new BigDecimal("65"))));
        return goalResponse(goal);
    }

    @Transactional
    public GoalsRequest updateGoals(GoalsRequest request) {
        NutritionGoal goal = goals.findById(GOAL_ID)
                .orElseGet(() -> new NutritionGoal(GOAL_ID, request.calories(), request.protein(),
                        request.carbs(), request.fat()));
        goal.update(request.calories(), request.protein(), request.carbs(), request.fat());
        return goalResponse(goals.save(goal));
    }

    @Transactional
    public MealResponse createMeal(MealRequest request) {
        if (request.date().isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "future_meal", "Meals cannot be logged in the future.");
        }
        Meal meal = new Meal(UUID.randomUUID(), request.type().trim(), request.date(), request.time(),
                request.notes() == null ? "" : request.notes().trim());
        for (MealItemRequest item : request.items()) {
            FoodItem food = foodOrNotFound(item.foodId());
            meal.addItem(new MealItem(UUID.randomUUID(), food, item.quantity()));
        }
        return mealResponse(meals.save(meal));
    }

    @Transactional(readOnly = true)
    public List<MealResponse> listMeals(LocalDate date) {
        List<Meal> results = date == null ? meals.findAllByOrderByDateDescTimeAsc()
                : meals.findAllByDateOrderByTimeAsc(date);
        return results.stream().map(KitchenService::mealResponse).toList();
    }

    @Transactional
    public void deleteMeal(UUID id) {
        Meal meal = meals.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "meal_not_found", "Meal was not found."));
        meals.delete(meal);
    }

    @Transactional(readOnly = true)
    public DailySummary dailySummary(LocalDate date) {
        List<MealResponse> dayMeals = listMeals(date);
        NutritionTotals total = dayMeals.stream().map(MealResponse::totals)
                .reduce(NutritionTotals.zero(), NutritionTotals::plus);
        GoalsRequest target = goals.findById(GOAL_ID)
                .map(KitchenService::goalResponse)
                .orElse(new GoalsRequest(new BigDecimal("2000"), new BigDecimal("120"),
                        new BigDecimal("220"), new BigDecimal("65")));
        NutritionTotals remaining = new NutritionTotals(target.calories().subtract(total.calories()),
                target.protein().subtract(total.protein()), target.carbs().subtract(total.carbs()),
                target.fat().subtract(total.fat()));
        return new DailySummary(date, total, target, remaining, dayMeals);
    }

    @Transactional(readOnly = true)
    public List<PantryResponse> listPantry() {
        return pantry.findAll().stream().map(KitchenService::pantryResponse)
                .sorted(Comparator.comparing(PantryResponse::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional
    public PantryResponse createPantryItem(PantryRequest request) {
        FoodItem food = foodOrNotFound(request.foodId());
        if (pantry.findByFoodId(food.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "pantry_item_exists",
                    "This food already has a pantry quantity. Adjust the existing item instead.");
        }
        return pantryResponse(pantry.save(new PantryItem(UUID.randomUUID(), food, request.quantity(),
                request.lowAt(), request.expires())));
    }

    @Transactional
    public PantryResponse updatePantryItem(UUID id, PantryRequest request) {
        PantryItem item = pantry.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "pantry_item_not_found", "Pantry item was not found."));
        if (!item.getFood().getId().equals(request.foodId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "pantry_food_mismatch",
                    "A pantry item's food cannot be changed; remove it and add the correct food.");
        }
        item.update(request.quantity(), request.lowAt(), request.expires());
        return pantryResponse(item);
    }

    @Transactional
    public PantryResponse adjustPantry(UUID id, PantryAdjustment request) {
        PantryItem item = pantry.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "pantry_item_not_found", "Pantry item was not found."));
        BigDecimal delta = request.add() ? request.quantity() : request.quantity().negate();
        BigDecimal result = item.getQuantity().add(delta);
        if (result.signum() < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "insufficient_stock",
                    "The requested amount exceeds the pantry quantity.");
        }
        if (result.compareTo(new BigDecimal("100000")) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "stock_limit_exceeded",
                    "The updated pantry quantity exceeds the maximum.");
        }
        item.adjust(delta);
        return pantryResponse(item);
    }

    @Transactional
    public void deletePantryItem(UUID id) {
        if (!pantry.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "pantry_item_not_found", "Pantry item was not found.");
        }
        pantry.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> listRecipes() {
        return recipes.findAllByOrderByNameAsc().stream().map(this::recipeResponse).toList();
    }

    @Transactional
    public RecipeResponse createRecipe(RecipeRequest request) {
        Recipe recipe = new Recipe(UUID.randomUUID(), request.name().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.prepMinutes(), request.servings());
        for (RecipeIngredientRequest item : request.items()) {
            recipe.addItem(new RecipeIngredient(UUID.randomUUID(), foodOrNotFound(item.foodId()), item.quantity()));
        }
        return recipeResponse(recipes.save(recipe));
    }

    @Transactional
    public void deleteRecipe(UUID id) {
        if (!recipes.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "recipe_not_found", "Recipe was not found.");
        }
        recipes.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommendations(LocalDate date) {
        DailySummary summary = dailySummary(date);
        BigDecimal remainingProtein = summary.remaining().protein().max(ZERO);
        return recipes.findAllByOrderByNameAsc().stream()
                .map(this::recipeResponse)
                .sorted(Comparator.comparing((RecipeResponse recipe) -> recipe.ingredientsAvailable() ? 0 : 1)
                        .thenComparing(recipe -> remainingProtein.subtract(recipe.nutritionPerServing().protein())
                                .abs()))
                .limit(3)
                .map(recipe -> {
                    List<String> reasons = new ArrayList<>();
                    reasons.add(recipe.ingredientsAvailable()
                            ? "You have the ingredients in your pantry."
                            : "Some ingredients may be missing.");
                    if (summary.totals().protein().compareTo(summary.goals().protein()) < 0) {
                        reasons.add("Adds protein to your day.");
                    }
                    return new RecommendationResponse(recipe, reasons);
                }).toList();
    }

    @Transactional
    public void importKitchenData(KitchenImportRequest request) {
        Map<String, ImportedFood> requestedFoods = request.foods().stream()
                .collect(Collectors.toMap(ImportedFood::id, Function.identity(), (first, duplicate) -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "duplicate_import_id",
                            "The backup contains duplicate food identifiers.");
                }));
        long distinctNames = request.foods().stream()
                .map(food -> food.name().trim().toLowerCase(java.util.Locale.ROOT)).distinct().count();
        if (distinctNames != request.foods().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "duplicate_food_name",
                    "The backup contains food names that differ only by letter case.");
        }
        validateImportReferences(request, requestedFoods.keySet());
        if (request.meals().stream().anyMatch(meal -> meal.date().isAfter(LocalDate.now()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "future_meal", "Meals cannot be logged in the future.");
        }

        meals.deleteAll(meals.findAll());
        recipes.deleteAll(recipes.findAll());
        pantry.deleteAll(pantry.findAll());
        foods.deleteAll(foods.findAll());
        goals.deleteAll(goals.findAll());

        Map<String, FoodItem> importedFoods = request.foods().stream().collect(Collectors.toMap(
                ImportedFood::id,
                imported -> foods.save(new FoodItem(UUID.randomUUID(), imported.name().trim(),
                        imported.category().trim(), imported.unit().trim(), imported.calories(),
                        imported.protein(), imported.carbs(), imported.fat(), imported.confidence().trim()))));

        goals.save(new NutritionGoal(GOAL_ID, request.goals().calories(), request.goals().protein(),
                request.goals().carbs(), request.goals().fat()));

        for (ImportedMeal imported : request.meals()) {
            Meal meal = new Meal(UUID.randomUUID(), imported.type().trim(), imported.date(), imported.time(),
                    imported.notes() == null ? "" : imported.notes().trim());
            for (ImportedMealItem item : imported.items()) {
                meal.addItem(new MealItem(UUID.randomUUID(), importedFoods.get(item.foodId()),
                        item.name().trim(), item.unit().trim(), item.quantity(), item.calories(),
                        item.protein(), item.carbs(), item.fat()));
            }
            meals.save(meal);
        }

        for (ImportedPantryItem imported : request.pantry()) {
            pantry.save(new PantryItem(UUID.randomUUID(), importedFoods.get(imported.foodId()),
                    imported.quantity(), imported.lowAt(), imported.expires()));
        }

        for (ImportedRecipe imported : request.recipes()) {
            Recipe recipe = new Recipe(UUID.randomUUID(), imported.name().trim(),
                    imported.description() == null ? "" : imported.description().trim(),
                    imported.prepMinutes(), imported.servings());
            for (ImportedRecipeIngredient item : imported.items()) {
                recipe.addItem(new RecipeIngredient(UUID.randomUUID(), importedFoods.get(item.foodId()),
                        item.quantity()));
            }
            recipes.save(recipe);
        }
    }

    private static void validateImportReferences(KitchenImportRequest request, java.util.Set<String> foodIds) {
        boolean invalidMealReference = request.meals().stream()
                .flatMap(meal -> meal.items().stream()).anyMatch(item -> !foodIds.contains(item.foodId()));
        boolean invalidPantryReference = request.pantry().stream()
                .anyMatch(item -> !foodIds.contains(item.foodId()));
        boolean invalidRecipeReference = request.recipes().stream()
                .flatMap(recipe -> recipe.items().stream()).anyMatch(item -> !foodIds.contains(item.foodId()));
        if (invalidMealReference || invalidPantryReference || invalidRecipeReference) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_food_reference",
                    "Every meal, pantry item, and recipe must refer to a food in the backup.");
        }
        long distinctPantryFoods = request.pantry().stream().map(ImportedPantryItem::foodId).distinct().count();
        if (distinctPantryFoods != request.pantry().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "duplicate_pantry_food",
                    "The backup contains more than one pantry item for the same food.");
        }
    }

    private void ensureUniqueFoodName(String name, UUID excludingId) {
        boolean exists = excludingId == null ? foods.existsByNameIgnoreCase(name.trim())
                : foods.existsByNameIgnoreCaseAndIdNot(name.trim(), excludingId);
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "duplicate_food_name",
                    "A food with that name already exists.");
        }
    }

    private FoodItem foodOrNotFound(UUID id) {
        return foods.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "food_not_found", "Food item was not found."));
    }

    private static FoodResponse foodResponse(FoodItem food) {
        return new FoodResponse(food.getId(), food.getName(), food.getCategory(), food.getUnit(), food.getCalories(),
                food.getProtein(), food.getCarbs(), food.getFat(), food.getConfidence());
    }

    private static GoalsRequest goalResponse(NutritionGoal goal) {
        return new GoalsRequest(goal.getCalories(), goal.getProtein(), goal.getCarbs(), goal.getFat());
    }

    private static MealResponse mealResponse(Meal meal) {
        List<MealItemResponse> items = meal.getItems().stream().map(item -> new MealItemResponse(
                item.getFood().getId(), item.getFoodName(), item.getUnit(), item.getQuantity(),
                item.getCalories(), item.getProtein(), item.getCarbs(), item.getFat())).toList();
        NutritionTotals total = items.stream().map(KitchenService::itemTotals)
                .reduce(NutritionTotals.zero(), NutritionTotals::plus);
        return new MealResponse(meal.getId(), meal.getType(), meal.getDate(), meal.getTime(), meal.getNotes(),
                items, total);
    }

    private static NutritionTotals itemTotals(MealItemResponse item) {
        return new NutritionTotals(item.calories().multiply(item.quantity()),
                item.protein().multiply(item.quantity()), item.carbs().multiply(item.quantity()),
                item.fat().multiply(item.quantity()));
    }

    private static PantryResponse pantryResponse(PantryItem item) {
        FoodItem food = item.getFood();
        return new PantryResponse(item.getId(), food.getId(), food.getName(), food.getCategory(), food.getUnit(),
                item.getQuantity(), item.getLowAt(), item.getExpires(), item.getQuantity().compareTo(item.getLowAt()) <= 0);
    }

    private RecipeResponse recipeResponse(Recipe recipe) {
        BigDecimal divisor = BigDecimal.valueOf(recipe.getServings());
        NutritionTotals total = NutritionTotals.zero();
        List<RecipeIngredientResponse> items = new ArrayList<>();
        boolean available = true;
        Map<UUID, PantryItem> stock = pantry.findAll().stream()
                .collect(Collectors.toMap(item -> item.getFood().getId(), Function.identity()));
        for (RecipeIngredient ingredient : recipe.getItems()) {
            FoodItem food = ingredient.getFood();
            items.add(new RecipeIngredientResponse(food.getId(), food.getName(), food.getUnit(),
                    ingredient.getQuantity()));
            BigDecimal factor = ingredient.getQuantity().divide(divisor, 8, RoundingMode.HALF_UP);
            total = total.plus(new NutritionTotals(food.getCalories().multiply(factor),
                    food.getProtein().multiply(factor), food.getCarbs().multiply(factor),
                    food.getFat().multiply(factor)));
            PantryItem pantryItem = stock.get(food.getId());
            if (pantryItem == null || pantryItem.getQuantity().compareTo(factor) < 0) {
                available = false;
            }
        }
        return new RecipeResponse(recipe.getId(), recipe.getName(), recipe.getDescription(),
                recipe.getPrepMinutes(), recipe.getServings(), items, round(total), available);
    }

    private static NutritionTotals round(NutritionTotals total) {
        return new NutritionTotals(total.calories().setScale(2, RoundingMode.HALF_UP),
                total.protein().setScale(2, RoundingMode.HALF_UP),
                total.carbs().setScale(2, RoundingMode.HALF_UP), total.fat().setScale(2, RoundingMode.HALF_UP));
    }
}
