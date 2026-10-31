# NutriKitchen API

The API is a single-user, unauthenticated REST API under `/api`. The Spring Boot app also serves the static browser UI. Send JSON with `Content-Type: application/json`; dates use ISO `YYYY-MM-DD` and times use `HH:mm:ss` or `HH:mm`.

| Method and path | Purpose |
|---|---|
| `GET /api/health` | Application readiness |
| `GET /api/goals` | Get the daily nutrition targets, creating defaults on first use |
| `PUT /api/goals` | Replace calorie and macro targets |
| `GET /api/foods` | List foods |
| `POST /api/foods` | Create a food with nutrients per serving unit |
| `PUT /api/foods/{id}` | Update a food; existing meal nutrition snapshots do not change |
| `DELETE /api/foods/{id}` | Delete an unused food; returns `409` if meal history references it |
| `GET /api/meals?date=YYYY-MM-DD` | List meals for a date; omit `date` to list all meals |
| `POST /api/meals` | Log a meal with one or more food IDs and positive quantities |
| `DELETE /api/meals/{id}` | Delete a meal and recalculate future summaries from remaining records |
| `GET /api/daily-summary?date=YYYY-MM-DD` | Calculate daily totals, goals, remaining targets, and meals |
| `GET /api/pantry` | List pantry estimates and low-stock status |
| `POST /api/pantry` | Add one pantry estimate for a food |
| `PUT /api/pantry/{id}` | Correct its quantity, reminder threshold, or expiry |
| `PATCH /api/pantry/{id}/adjust` | Add or use a quantity, with concurrent-update protection |
| `DELETE /api/pantry/{id}` | Remove an inventory estimate |
| `GET /api/recipes` | List recipes with per-serving nutrition and ingredient availability |
| `POST /api/recipes` | Save a recipe referencing existing food IDs |
| `DELETE /api/recipes/{id}` | Delete a recipe |
| `GET /api/recommendations?date=YYYY-MM-DD` | Return up to three ingredient-aware recipe suggestions |
| `POST /api/data/import` | Atomically replace all application data from a validated browser backup |

Nutrition totals are derived from meal-item snapshots. Changing a food later does not rewrite a meal's historical nutrient values. Pantry amounts are estimates and are not deducted automatically when a meal or recipe is logged. Recipe ingredient quantities use the serving unit defined by each food.

Validation errors return `400`; missing records return `404`; duplicate names, invalid stock adjustments, and protected food deletion return `409`. Error bodies use `{ "code", "message", "timestamp", "details" }`.

The import endpoint replaces goals, foods, meals, pantry, and recipes in one transaction. Invalid food references or values reject the whole import without replacing existing data. The API does not include authentication or authorization yet; keep it on localhost or a trusted private network.
