# MVP scope and requirements

## 1. MVP scope statement

The first release is a single-user nutrition tracker that lets a user:

- create and maintain a personal list of food items,
- log meals with one or more food items,
- calculate macro totals for a meal and across the day,
- compare totals against daily nutrition targets,
- review a simple record of what was eaten and when.

Out of scope for the MVP:

- multi-user accounts,
- authentication and authorization,
- grocery import integrations,
- pantry inventory corrections and recipe consumption logic,
- recommendation engine,
- AI-assisted data entry.

## 2. Glossary and domain definitions

### User
A single person using the product. For MVP, the app is implicitly tied to one user account or a single local profile. The user defines their nutrition goals and meal history.

### FoodItem
A known food or ingredient in the system. Each food item has a name, optional brand or category, a serving unit, and nutrient values per serving unit. Nutrient values may be exact or estimated.

### Meal
A meal event representing food consumed in a time window (for example: breakfast, lunch, dinner, snack, restaurant meal). A meal contains one or more `MealItem` entries and is associated with a date/time and an optional meal source.

### MealItem
A line item inside a meal, linking a `Meal` to a `FoodItem` and recording the quantity consumed. It captures the data needed for calculating the meal's nutrition totals.

### NutritionGoal
A daily target for calories and/or macro values (for example protein, carbohydrate, fats). It may be expressed per day and is attached to a user.

## 3. Functional requirements

1. The system shall allow a user to create a food item with a unique name and a serving unit.
2. The system shall allow a user to define nutrient values for a food item per serving unit.
3. The system shall allow a user to edit or delete a food item if it is not referenced by a meal history that would become ambiguous.
4. The system shall allow a user to log a meal with a date/time, meal type, and optional notes.
5. The system shall allow a user to add multiple food items to one meal.
6. The system shall require a positive numeric quantity for each meal item.
7. The system shall calculate the total macro and calorie values for a meal from its `MealItem`s.
8. The system shall aggregate totals by day for the user.
9. The system shall compare daily totals against the user's nutrition goals.
10. The system shall show at least the remaining calories and macros to goal on a given day.

## 4. Non-functional requirements

- The application must validate required data before persisting it.
- Numeric values must use sensible precision and reject negative or impossible values.
- APIs or service logic must fail clearly when required fields are absent or invalid.
- The data model must support future addition of pantry, recipes, and integrations without major rework.
- The system should be fast enough for a single-user local use case to feel immediate.
- The code should be testable at the unit and integration levels.

## 5. Business rules

- A meal item cannot have a zero or negative quantity.
- A nutrient value for a food item should be represented as a numerical value with a source confidence or estimate flag when exact data is not known.
- Meal totals must be derived from meal items rather than stored separately unless a denormalized view is explicitly introduced later.
- Daily totals are calculated from logged meals and not inferred from purchases or inventory.
- If a nutrient value is unknown, it must be handled without silently producing misleading totals.
- A user can define different daily goals, but the initial release should prioritize a single daily goal set per user.

## 6. Edge cases and failure cases

- Food item name duplicated with different units or brands should be handled deliberately, either by enforcing unique names or by requiring a stronger identity strategy.
- A user tries to save a meal with no items.
- A user enters a meal item quantity that is extremely large or malformed.
- A user logs the same meal twice due to a retry or duplicate action.
- A food item has missing nutrient values and is used in meal logging.
- A user updates a food item after it was already used in historical meals.
- A user enters meal date/time in the future.
- A user deletes a meal after totals are already reported; totals should be recalculated from persisted data.

## 7. Acceptance criteria

### AC1: Create a food item
Given a user wants to add a food item,
When they provide a name, serving unit, and at least a minimal nutrient set,
Then the system saves the food item and can later be used in a meal.

### AC2: Log a meal
Given a user has created food items,
When they record a meal with multiple items and quantities,
Then the system saves the meal and totals the calories and macros correctly.

### AC3: Daily totals
Given a user has logged multiple meals for the same day,
When the user views the day's summary,
Then the app shows the summed calories and macros for that day.

### AC4: Goal comparison
Given a user has set daily nutrition goals,
When they view the daily totals,
Then the application shows progress toward or remaining values to meet the goal.

### AC5: Validation
Given invalid data such as a negative quantity or missing required values,
When the user submits the meal or food item,
Then the system rejects the request with a clear validation error.

## 8. Open questions to confirm

- Is the initial solution single-user only, or should user identity be modeled even if there is no auth yet?
- Should a food item have a unique name globally, or by user?
- What nutrients are mandatory for the MVP: calories, protein, carbs, fats only, or additional nutrients such as fiber, sodium, sugar, and micronutrients?
- Can a meal be either planned or consumed, or only consumed in the MVP?
- What is the minimum acceptable precision and rounding strategy for nutrition totals?
- Should a meal item allow an estimated quantity or only exact values?

## 9. MVP success measure

The MVP is successful when a user can add a few known foods, log one or more meals, and see the day's totals and remaining goals without manual spreadsheet math.
