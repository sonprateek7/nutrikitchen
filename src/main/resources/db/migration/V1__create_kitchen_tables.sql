CREATE TABLE food_items (
    id UUID PRIMARY KEY,
    name VARCHAR(70) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL,
    serving_unit VARCHAR(16) NOT NULL,
    calories NUMERIC(10, 3) NOT NULL CHECK (calories >= 0),
    protein NUMERIC(10, 3) NOT NULL CHECK (protein >= 0),
    carbs NUMERIC(10, 3) NOT NULL CHECK (carbs >= 0),
    fat NUMERIC(10, 3) NOT NULL CHECK (fat >= 0),
    source_confidence VARCHAR(30) NOT NULL
);

CREATE TABLE nutrition_goals (
    id BIGINT PRIMARY KEY,
    calories NUMERIC(10, 2) NOT NULL CHECK (calories > 0),
    protein NUMERIC(10, 2) NOT NULL CHECK (protein > 0),
    carbs NUMERIC(10, 2) NOT NULL CHECK (carbs > 0),
    fat NUMERIC(10, 2) NOT NULL CHECK (fat > 0)
);

CREATE TABLE meals (
    id UUID PRIMARY KEY,
    meal_type VARCHAR(20) NOT NULL,
    consumed_on DATE NOT NULL,
    consumed_at TIME NOT NULL,
    notes VARCHAR(160)
);

CREATE INDEX idx_meals_consumed_on_time ON meals (consumed_on, consumed_at);

CREATE TABLE meal_items (
    id UUID PRIMARY KEY,
    meal_id UUID NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    food_item_id UUID NOT NULL REFERENCES food_items(id) ON DELETE RESTRICT,
    food_name VARCHAR(70) NOT NULL,
    serving_unit VARCHAR(16) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    calories NUMERIC(10, 3) NOT NULL CHECK (calories >= 0),
    protein NUMERIC(10, 3) NOT NULL CHECK (protein >= 0),
    carbs NUMERIC(10, 3) NOT NULL CHECK (carbs >= 0),
    fat NUMERIC(10, 3) NOT NULL CHECK (fat >= 0)
);

CREATE INDEX idx_meal_items_food_id ON meal_items (food_item_id);

CREATE TABLE pantry_items (
    id UUID PRIMARY KEY,
    food_item_id UUID NOT NULL UNIQUE REFERENCES food_items(id) ON DELETE CASCADE,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity >= 0),
    low_at NUMERIC(12, 3) NOT NULL CHECK (low_at >= 0),
    expires DATE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE recipes (
    id UUID PRIMARY KEY,
    name VARCHAR(70) NOT NULL,
    description VARCHAR(240),
    prep_minutes INTEGER NOT NULL CHECK (prep_minutes > 0),
    servings INTEGER NOT NULL CHECK (servings > 0)
);

CREATE TABLE recipe_ingredients (
    id UUID PRIMARY KEY,
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    food_item_id UUID NOT NULL REFERENCES food_items(id) ON DELETE RESTRICT,
    quantity NUMERIC(12, 3) NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_recipe_ingredients_food_id ON recipe_ingredients (food_item_id);
