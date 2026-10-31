(() => {
  "use strict";

  const STORAGE_KEY = "nutrikitchen.data.v1";
  const API_BASE_URL = String(window.NUTRIKITCHEN_API_BASE || "").replace(/\/+$/, "");
  const today = localDateString(new Date());
  let hasSavedLocalData = false;
  let backendMode = false;
  let backendConnectionMessage = "";
  let storageWarning = "";
  let storageReadOnly = false;
  let storageWarningShown = false;
  let unreadableData = "";
  const starterData = {
    goals: { calories: 2000, protein: 120, carbs: 220, fat: 65 },
    foods: [
      { id: "food-oats", name: "Rolled oats", category: "Grains", unit: "g", calories: 3.89, protein: 0.169, carbs: 0.663, fat: 0.069, confidence: "Estimated" },
      { id: "food-yogurt", name: "Greek yogurt", category: "Dairy", unit: "g", calories: 0.97, protein: 0.1, carbs: 0.036, fat: 0.035, confidence: "Estimated" },
      { id: "food-blueberry", name: "Blueberries", category: "Fruit", unit: "g", calories: 0.57, protein: 0.007, carbs: 0.145, fat: 0.003, confidence: "Estimated" },
      { id: "food-eggs", name: "Eggs", category: "Protein", unit: "piece", calories: 72, protein: 6.3, carbs: 0.4, fat: 4.8, confidence: "Estimated" },
      { id: "food-avocado", name: "Avocado", category: "Produce", unit: "g", calories: 1.6, protein: 0.02, carbs: 0.085, fat: 0.147, confidence: "Estimated" },
      { id: "food-chicken", name: "Chicken breast", category: "Protein", unit: "g", calories: 1.65, protein: 0.31, carbs: 0, fat: 0.036, confidence: "Estimated" },
      { id: "food-rice", name: "Brown rice, cooked", category: "Grains", unit: "g", calories: 1.23, protein: 0.027, carbs: 0.256, fat: 0.01, confidence: "Estimated" },
      { id: "food-spinach", name: "Baby spinach", category: "Produce", unit: "g", calories: 0.23, protein: 0.029, carbs: 0.036, fat: 0.004, confidence: "Estimated" }
    ],
    meals: [
      { id: "meal-demo-1", type: "Breakfast", date: today, time: "08:15", notes: "", items: [{ foodId: "food-oats", name: "Rolled oats", unit: "g", quantity: 50, calories: 3.89, protein: 0.169, carbs: 0.663, fat: 0.069 }, { foodId: "food-yogurt", name: "Greek yogurt", unit: "g", quantity: 150, calories: 0.97, protein: 0.1, carbs: 0.036, fat: 0.035 }, { foodId: "food-blueberry", name: "Blueberries", unit: "g", quantity: 60, calories: 0.57, protein: 0.007, carbs: 0.145, fat: 0.003 }] },
      { id: "meal-demo-2", type: "Lunch", date: today, time: "12:30", notes: "", items: [{ foodId: "food-chicken", name: "Chicken breast", unit: "g", quantity: 140, calories: 1.65, protein: 0.31, carbs: 0, fat: 0.036 }, { foodId: "food-rice", name: "Brown rice, cooked", unit: "g", quantity: 120, calories: 1.23, protein: 0.027, carbs: 0.256, fat: 0.01 }, { foodId: "food-spinach", name: "Baby spinach", unit: "g", quantity: 40, calories: 0.23, protein: 0.029, carbs: 0.036, fat: 0.004 }] }
    ],
    pantry: [
      { id: "stock-oats", foodId: "food-oats", quantity: 420, lowAt: 100, expires: "" },
      { id: "stock-yogurt", foodId: "food-yogurt", quantity: 260, lowAt: 100, expires: "" },
      { id: "stock-eggs", foodId: "food-eggs", quantity: 5, lowAt: 2, expires: "" },
      { id: "stock-avocado", foodId: "food-avocado", quantity: 180, lowAt: 80, expires: "" },
      { id: "stock-chicken", foodId: "food-chicken", quantity: 350, lowAt: 150, expires: "" },
      { id: "stock-rice", foodId: "food-rice", quantity: 500, lowAt: 150, expires: "" }
    ],
    recipes: [
      { id: "recipe-bowl", name: "Blueberry breakfast bowl", description: "A bright, protein-rich start with oats, yogurt, and berries.", prepMinutes: 10, servings: 1, items: [{ foodId: "food-oats", quantity: 50 }, { foodId: "food-yogurt", quantity: 150 }, { foodId: "food-blueberry", quantity: 60 }] },
      { id: "recipe-lunch", name: "Everyday chicken bowl", description: "Simple chicken and brown rice with a handful of greens.", prepMinutes: 25, servings: 1, items: [{ foodId: "food-chicken", quantity: 140 }, { foodId: "food-rice", quantity: 120 }, { foodId: "food-spinach", quantity: 40 }] }
    ]
  };

  let state = loadState();
  let currentPage = location.hash.slice(1) || "dashboard";
  let selectedDate = today;
  let searchTerm = "";
  let categoryFilter = "All categories";
  const app = document.querySelector("#app");
  const modalRoot = document.querySelector("#modal-root");
  modalRoot.addEventListener("click", (event) => {
    if (event.target.classList.contains("modal-backdrop")) closeModal();
  });
  const pageTitles = { dashboard: "Overview", journal: "Meal journal", foods: "My foods", pantry: "My pantry", recipes: "Recipes" };
  const pageEmojis = { Breakfast: "☀️", Lunch: "🥗", Dinner: "🍲", Snack: "🍎", Other: "🍽️" };
  const categoryEmojis = { Grains: "🌾", Dairy: "🥛", Fruit: "🫐", Protein: "🥚", Produce: "🥬", Other: "🍴" };

  function localDateString(date) {
    const offset = date.getTimezoneOffset();
    return new Date(date.getTime() - offset * 60000).toISOString().slice(0, 10);
  }

  function cloneData(value) {
    return JSON.parse(JSON.stringify(value));
  }

  function loadState() {
    let saved;
    try {
      saved = localStorage.getItem(STORAGE_KEY);
    } catch (error) {
      console.error("Could not read NutriKitchen data.", error);
      storageReadOnly = true;
      storageWarning = "Browser storage is unavailable. Your changes cannot be saved; try enabling site data in your browser.";
      return cloneData(starterData);
    }
    if (!saved) return cloneData(starterData);
    try {
      const parsed = JSON.parse(saved);
      const restored = { ...cloneData(starterData), ...parsed };
      if (!validateImportedState(restored)) {
        throw new Error("Saved kitchen data has an unexpected format.");
      }
      hasSavedLocalData = true;
      return restored;
    } catch (error) {
      console.error("Could not load NutriKitchen data.", error);
      storageReadOnly = true;
      unreadableData = saved;
      storageWarning = "Saved data could not be read. Import a valid backup or reset local data to continue saving.";
      storageWarningShown = false;
      return cloneData(starterData);
    }
  }

  function persist() {
    if (storageReadOnly && !backendMode) {
      showToast("Saving is paused. Import a valid backup or reset local data first.");
      return false;
    }
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (error) {
      console.error("Could not save NutriKitchen data.", error);
      showToast(backendMode ? "Saved to the server, but this browser could not update its offline cache." : "Your browser could not save this change. Check available storage.");
      return backendMode;
    }
    hasSavedLocalData = true;
    return true;
  }

  async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}/api${path}`, {
      ...options,
      headers: { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers }
    });
    if (response.status === 204) return null;
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      const error = new Error(payload?.message || `The server returned ${response.status}.`);
      error.status = response.status;
      error.code = payload?.code;
      throw error;
    }
    return payload;
  }

  async function refreshBackendState() {
    const [goals, foods, meals, pantry, recipes, recommendations, dailySummary] = await Promise.all([
      apiRequest("/goals"),
      apiRequest("/foods"),
      apiRequest("/meals"),
      apiRequest("/pantry"),
      apiRequest("/recipes"),
      apiRequest(`/recommendations?date=${encodeURIComponent(selectedDate)}`),
      apiRequest(`/daily-summary?date=${encodeURIComponent(selectedDate)}`)
    ]);
    state = {
      goals,
      foods,
      meals: meals.map(normalizeMeal),
      pantry: pantry.map((item) => ({ id: item.id, foodId: item.foodId, quantity: item.quantity, lowAt: item.lowAt, expires: item.expires || "" })),
      recipes: recipes.map(normalizeRecipe),
      recommendations,
      dailySummary
    };
    storageReadOnly = false;
    storageWarning = "";
    unreadableData = "";
    persist();
  }

  async function refreshRecommendations() {
    if (!backendMode) return;
    try {
      const [recommendations, dailySummary] = await Promise.all([
        apiRequest(`/recommendations?date=${encodeURIComponent(selectedDate)}`),
        apiRequest(`/daily-summary?date=${encodeURIComponent(selectedDate)}`)
      ]);
      state.recommendations = recommendations;
      state.dailySummary = dailySummary;
      render();
    } catch (error) {
      showApiFailure(error);
    }
  }

  function showApiFailure(error) {
    console.error("NutriKitchen API request failed.", error);
    showToast(error.message || "The server could not save this change.");
  }

  function normalizeMeal(meal) {
    return { id: meal.id, type: meal.type, date: meal.date, time: meal.time, notes: meal.notes || "", items: meal.items };
  }

  function normalizeRecipe(recipe) {
    return { id: recipe.id, name: recipe.name, description: recipe.description || "", prepMinutes: recipe.prepMinutes, servings: recipe.servings, items: recipe.items };
  }

  function localDataForImport(data) {
    return {
      goals: data.goals,
      foods: data.foods,
      meals: data.meals.map((meal) => ({
        id: meal.id, type: meal.type, date: meal.date, time: meal.time, notes: meal.notes || "",
        items: meal.items.map((item) => ({ foodId: item.foodId, name: item.name, unit: item.unit, quantity: item.quantity, calories: item.calories, protein: item.protein, carbs: item.carbs, fat: item.fat }))
      })),
      pantry: data.pantry.map((item) => ({ ...item, expires: item.expires || null })),
      recipes: data.recipes.map((recipe) => ({ ...recipe, items: recipe.items.map((item) => ({ ...item })) }))
    };
  }

  async function connectBackend() {
    try {
      await apiRequest("/health");
      backendMode = true;
      const [foods, meals, pantry, recipes] = await Promise.all([
        apiRequest("/foods"), apiRequest("/meals"), apiRequest("/pantry"), apiRequest("/recipes")
      ]);
      if (hasSavedLocalData && !foods.length && !meals.length && !pantry.length && !recipes.length) {
        await apiRequest("/data/import", { method: "POST", body: JSON.stringify(localDataForImport(cloneData(state))) });
      }
      await refreshBackendState();
      backendConnectionMessage = "";
      render();
      showToast("Connected to your NutriKitchen server.");
    } catch (error) {
      backendMode = false;
      if (API_BASE_URL || !location.hostname.endsWith("github.io")) {
        console.error("NutriKitchen backend is unavailable.", error);
      }
      if (API_BASE_URL) {
        backendConnectionMessage = `The configured API could not be reached (${error.message}). Changes are staying in this browser for now.`;
        render();
      }
    }
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
  }

  function number(value, places = 0) {
    return new Intl.NumberFormat(undefined, { maximumFractionDigits: places, minimumFractionDigits: 0 }).format(value || 0);
  }

  function dateLabel(date, options = { weekday: "long", month: "long", day: "numeric" }) {
    return new Intl.DateTimeFormat(undefined, options).format(new Date(`${date}T12:00:00`));
  }

  function getFood(id) {
    return state.foods.find((food) => food.id === id);
  }

  function createId() {
    return globalThis.crypto?.randomUUID ? globalThis.crypto.randomUUID() : `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
  }

  function itemNutrition(item) {
    const quantity = Number(item.quantity) || 0;
    return {
      calories: quantity * item.calories,
      protein: quantity * item.protein,
      carbs: quantity * item.carbs,
      fat: quantity * item.fat
    };
  }

  function mealNutrition(meal) {
    return meal.items.reduce((total, item) => {
      const values = itemNutrition(item);
      return { calories: total.calories + values.calories, protein: total.protein + values.protein, carbs: total.carbs + values.carbs, fat: total.fat + values.fat };
    }, { calories: 0, protein: 0, carbs: 0, fat: 0 });
  }

  function totalsFor(date) {
    return state.meals.filter((meal) => meal.date === date).reduce((total, meal) => {
      const values = mealNutrition(meal);
      return { calories: total.calories + values.calories, protein: total.protein + values.protein, carbs: total.carbs + values.carbs, fat: total.fat + values.fat };
    }, { calories: 0, protein: 0, carbs: 0, fat: 0 });
  }

  function progress(value, goal) {
    return goal > 0 ? Math.min(value / goal * 100, 100) : 0;
  }

  function fmtQuantity(quantity, unit) {
    return `${number(quantity, 1)} ${escapeHtml(unit)}`;
  }

  function showToast(message) {
    const region = document.querySelector("#toast-region");
    const toast = document.createElement("div");
    toast.className = "toast";
    toast.textContent = message;
    region.append(toast);
    window.setTimeout(() => toast.remove(), 3200);
  }

  function render() {
    document.querySelector("#page-title").textContent = pageTitles[currentPage] || pageTitles.dashboard;
    document.querySelector("#today-label").textContent = dateLabel(today, { month: "short", day: "numeric", year: "numeric" });
    document.querySelectorAll(".main-nav a").forEach((link) => link.classList.toggle("active", link.dataset.page === currentPage));
    document.querySelector("#sidebar").classList.remove("open");
    const renderers = { dashboard: renderDashboard, journal: renderJournal, foods: renderFoods, pantry: renderPantry, recipes: renderRecipes };
    app.innerHTML = `${backendMode ? `<div class="notice" style="margin-bottom:15px"><strong>Connected to your NutriKitchen server.</strong> Your data is saved to its database.</div>` : backendConnectionMessage ? `<div class="notice" style="margin-bottom:15px"><strong>Backend unavailable.</strong> ${escapeHtml(backendConnectionMessage)}</div>` : ""}${storageReadOnly ? `<div class="notice" style="margin-bottom:15px"><strong>Local storage needs attention.</strong> ${escapeHtml(storageWarning)} <button class="link-button" data-action="reset-data">Reset local data</button></div>` : ""}${(renderers[currentPage] || renderDashboard)()}`;
    if (storageWarning && !storageWarningShown) {
      showToast(storageWarning);
      storageWarningShown = true;
    }
  }

  function renderHeading(title, subtitle, actionText, action) {
    return `<div class="page-heading"><div><p class="eyebrow">YOUR KITCHEN</p><h1>${title}</h1><p>${subtitle}</p></div>${actionText ? `<button class="button button-primary" data-action="${action}"><span>＋</span> ${actionText}</button>` : ""}</div>`;
  }

  function renderDashboard() {
    const totals = backendMode && state.dailySummary?.date === selectedDate
      ? state.dailySummary.totals : totalsFor(selectedDate);
    const goals = state.goals;
    const meals = state.meals.filter((meal) => meal.date === selectedDate).sort((a, b) => a.time.localeCompare(b.time));
    const remaining = goals.calories - totals.calories;
    const greeting = selectedDate === today ? "A little nourishment goes a long way." : "Here's how your day is shaping up.";
    const macro = (label, key, color, unit) => `<div class="macro-row"><div class="macro-caption"><span>${label}</span><strong>${number(totals[key])} <span style="color:#919a91;font-weight:500">/ ${number(goals[key])} ${unit}</span></strong></div><div class="macro-track"><div class="macro-fill ${color}" style="width:${progress(totals[key], goals[key])}%"></div></div></div>`;
    const mealRows = meals.length ? meals.map((meal) => {
      const nutrition = mealNutrition(meal);
      return `<div class="meal-row"><span class="meal-emoji">${pageEmojis[meal.type] || pageEmojis.Other}</span><span class="meal-details"><strong>${escapeHtml(meal.type)}</strong><small>${escapeHtml(meal.items.map((item) => item.name).join(", "))}</small></span><span class="meal-calories"><strong>${number(nutrition.calories)} kcal</strong><small>${escapeHtml(meal.time)}</small></span></div>`;
    }).join("") : `<div class="empty-state"><span class="empty-icon">🥣</span><h3>Your day is a blank page</h3><p>Log a meal to start seeing your nutrition for the day.</p><button class="button button-secondary button-small" data-action="log-meal">Log your first meal</button></div>`;
    return `<div class="welcome-row"><div><p class="eyebrow">${selectedDate === today ? "A FRESH START" : "DAILY SNAPSHOT"}</p><h1>${selectedDate === today ? "Good food, good day." : escapeHtml(dateLabel(selectedDate, { weekday: "long" }))}</h1><p class="welcome-subtitle">${greeting}</p></div><label class="date-control"><span>◷</span><span>Viewing</span><input id="dashboard-date" type="date" value="${escapeHtml(selectedDate)}" max="${today}" aria-label="Select a day"></label></div>
      <div class="stats-grid">
        <article class="stat-card"><div class="stat-topline"><span class="stat-label">Calories eaten</span><span class="stat-icon">◒</span></div><div class="stat-value">${number(totals.calories)}<span class="stat-unit">kcal</span></div><div class="stat-foot"><strong>${number(Math.max(remaining, 0))}</strong> kcal ${remaining >= 0 ? "remaining" : "over goal"}</div></article>
        <article class="stat-card"><div class="stat-topline"><span class="stat-label">Protein</span><span class="stat-icon">↗</span></div><div class="stat-value">${number(totals.protein)}<span class="stat-unit">g</span></div><div class="stat-foot"><strong>${number(Math.max(goals.protein - totals.protein, 0))} g</strong> to your daily goal</div></article>
        <article class="stat-card"><div class="stat-topline"><span class="stat-label">Carbohydrates</span><span class="stat-icon">⌁</span></div><div class="stat-value">${number(totals.carbs)}<span class="stat-unit">g</span></div><div class="stat-foot"><strong>${number(Math.max(goals.carbs - totals.carbs, 0))} g</strong> to your daily goal</div></article>
        <article class="stat-card"><div class="stat-topline"><span class="stat-label">Fat</span><span class="stat-icon">✳</span></div><div class="stat-value">${number(totals.fat)}<span class="stat-unit">g</span></div><div class="stat-foot"><strong>${number(Math.max(goals.fat - totals.fat, 0))} g</strong> to your daily goal</div></article>
      </div>
      <div class="dashboard-grid">
        <div class="column-stack">
          <section class="card card-padding"><div class="card-heading"><div><h2>Your nutrition today</h2><p>A gentle guide, not a rulebook.</p></div><button class="link-button" data-action="edit-goals">Edit goals ↗</button></div><div class="macro-list">${macro("Protein", "protein", "", "g")}${macro("Carbohydrates", "carbs", "orange", "g")}${macro("Fat", "fat", "blue", "g")}</div><div class="macro-footer"><span>Calories</span><strong>${number(totals.calories)} / ${number(goals.calories)} kcal</strong></div></section>
          <section class="card card-padding"><div class="card-heading"><div><h2>Today's meals</h2><p>${meals.length} ${meals.length === 1 ? "meal" : "meals"} logged · ${dateLabel(selectedDate, { month: "short", day: "numeric" })}</p></div><button class="link-button" data-navigate="journal">View journal ↗</button></div><div class="meal-list">${mealRows}</div></section>
          <div class="tip-banner"><span class="tip-icon">🌿</span><p><strong>A gentle reminder</strong><br>Progress is built from patterns over time. One meal never defines your day.</p></div>
        </div>
        <div class="column-stack">
          <section class="card recommend-card"><div class="card-padding"><div class="card-heading"><div><h2>A good next bite</h2><p>Ideas from what you have on hand.</p></div><span class="stat-icon">✳</span></div><p class="recommend-intro">Simple ideas based on your pantry and today's nutrition. These are suggestions, not prescriptions.</p>${renderRecommendations(totals)}</div></section>
          <section class="card card-padding"><div class="card-heading"><div><h2>In your kitchen</h2><p>A quick look at your pantry.</p></div><button class="link-button" data-navigate="pantry">View pantry ↗</button></div>${renderPantrySnapshot()}</section>
        </div>
      </div>`;
  }

  function renderRecommendations(totals) {
    if (backendMode && Array.isArray(state.recommendations)) {
      if (!state.recommendations.length) return `<div class="empty-state"><span class="empty-icon">🍳</span><h3>Add a recipe to get ideas</h3><p>Your saved recipes will show here with ingredients you already have.</p><button class="button button-secondary button-small" data-navigate="recipes">Explore recipes</button></div>`;
      return state.recommendations.map(({ recipe, reasons }) => `<div class="recommendation"><div class="recommendation-top"><span class="recommendation-icon">🍽️</span><span><strong>${escapeHtml(recipe.name)}</strong><small>${number(recipe.nutritionPerServing.calories)} kcal · ${number(recipe.nutritionPerServing.protein)} g protein · ${recipe.prepMinutes} min</small></span></div><p class="recommendation-reason">${reasons.map(escapeHtml).join(" ")}</p></div>`).join("");
    }
    const candidates = state.recipes.map((recipe) => {
      const available = recipe.items.every((item) => {
        const stock = state.pantry.find((entry) => entry.foodId === item.foodId);
        return stock && stock.quantity >= item.quantity / recipe.servings;
      });
      const nutrients = recipeNutrition(recipe);
      const proteinGap = Math.max(state.goals.protein - totals.protein, 0);
      return { recipe, available, nutrients, score: (available ? 100 : 0) + Math.min(nutrients.protein, proteinGap) - nutrients.calories / 100 };
    }).sort((a, b) => b.score - a.score).slice(0, 2);
    if (!candidates.length) return `<div class="empty-state"><span class="empty-icon">🍳</span><h3>Add a recipe to get ideas</h3><p>Your saved recipes will show here with ingredients you already have.</p><button class="button button-secondary button-small" data-navigate="recipes">Explore recipes</button></div>`;
    return candidates.map(({ recipe, available, nutrients }) => `<div class="recommendation"><div class="recommendation-top"><span class="recommendation-icon">🍽️</span><span><strong>${escapeHtml(recipe.name)}</strong><small>${number(nutrients.calories)} kcal · ${number(nutrients.protein)} g protein · ${recipe.prepMinutes} min</small></span></div><p class="recommendation-reason">${available ? "You have the ingredients in your pantry." : "A few ingredients may be missing."} ${totals.protein < state.goals.protein ? "Adds a little protein to your day." : "A simple option for a balanced meal."}</p></div>`).join("");
  }

  function recipeNutrition(recipe) {
    return recipe.items.reduce((total, item) => {
      const food = getFood(item.foodId);
      if (!food) return total;
      const values = itemNutrition({ ...food, quantity: item.quantity / recipe.servings });
      return { calories: total.calories + values.calories, protein: total.protein + values.protein, carbs: total.carbs + values.carbs, fat: total.fat + values.fat };
    }, { calories: 0, protein: 0, carbs: 0, fat: 0 });
  }

  function renderPantrySnapshot() {
    const entries = state.pantry.slice(0, 4);
    if (!entries.length) return `<div class="empty-state"><span class="empty-icon">🧺</span><h3>Your pantry is waiting</h3><p>Add ingredients to see what's on hand.</p></div>`;
    return `<div class="meal-list">${entries.map((entry) => {
      const food = getFood(entry.foodId);
      if (!food) return "";
      const low = entry.quantity <= entry.lowAt;
      return `<div class="meal-row"><span class="meal-emoji">${categoryEmojis[food.category] || "🥬"}</span><span class="meal-details"><strong>${escapeHtml(food.name)}</strong><small>${low ? "Running low" : "In stock"}</small></span><span class="meal-calories"><strong>${number(entry.quantity, 1)} ${escapeHtml(food.unit)}</strong></span></div>`;
    }).join("")}</div>`;
  }

  function renderJournal() {
    const meals = state.meals.filter((meal) => meal.date === selectedDate).sort((a, b) => a.time.localeCompare(b.time));
    const totals = backendMode && state.dailySummary?.date === selectedDate
      ? state.dailySummary.totals : totalsFor(selectedDate);
    const content = meals.length ? meals.map((meal) => {
      const nutrition = mealNutrition(meal);
      return `<article class="journal-meal"><span class="meal-emoji">${pageEmojis[meal.type] || pageEmojis.Other}</span><div class="journal-content"><div class="journal-meal-head"><h3>${escapeHtml(meal.type)}</h3><span>${escapeHtml(meal.time)}</span></div><p class="journal-items">${meal.items.map((item) => `${escapeHtml(item.name)} · ${fmtQuantity(item.quantity, item.unit)}`).join("<br>")}${meal.notes ? `<br><em>${escapeHtml(meal.notes)}</em>` : ""}</p><div class="journal-meal-foot"><span>${number(nutrition.protein)} g protein · ${number(nutrition.carbs)} g carbs · ${number(nutrition.fat)} g fat</span><strong>${number(nutrition.calories)} kcal</strong></div></div><button class="row-action" data-action="delete-meal" data-id="${escapeHtml(meal.id)}" aria-label="Delete ${escapeHtml(meal.type)}">×</button></article>`;
    }).join("") : `<div class="empty-state"><span class="empty-icon">📝</span><h3>No meals logged for this day</h3><p>Start a gentle record of what you ate. You can always edit it later.</p><button class="button button-primary button-small" data-action="log-meal">Log a meal</button></div>`;
    return `${renderHeading("Meal journal", "A simple record of what you've enjoyed, whenever you want to keep one.", "Log a meal", "log-meal")}<div class="filter-row"><label class="date-control"><span>◷</span><span>Day</span><input id="journal-date" type="date" value="${escapeHtml(selectedDate)}" max="${today}" aria-label="Select journal day"></label><span class="pill">${number(totals.calories)} kcal · ${meals.length} ${meals.length === 1 ? "meal" : "meals"}</span></div><section class="card list-card"><div class="journal-date"><strong>${escapeHtml(dateLabel(selectedDate))}</strong><small>${number(totals.protein)} g protein · ${number(totals.carbs)} g carbs · ${number(totals.fat)} g fat</small></div>${content}</section>`;
  }

  function renderFoods() {
    const categories = ["All categories", ...new Set(state.foods.map((food) => food.category))];
    const filtered = state.foods.filter((food) => food.name.toLowerCase().includes(searchTerm.toLowerCase()) && (categoryFilter === "All categories" || food.category === categoryFilter));
    const rows = filtered.map((food) => `<tr><td><span class="food-name-cell"><span class="food-badge">${categoryEmojis[food.category] || "🍴"}</span>${escapeHtml(food.name)}</span></td><td>${escapeHtml(food.category)}</td><td>${number(food.calories, 1)} kcal</td><td>${number(food.protein, 1)} g</td><td>${number(food.carbs, 1)} g</td><td>${number(food.fat, 1)} g</td><td><span class="pill ${food.confidence === "Estimated" ? "warning" : ""}">${escapeHtml(food.confidence)}</span></td><td><button class="row-action" data-action="edit-food" data-id="${escapeHtml(food.id)}" aria-label="Edit ${escapeHtml(food.name)}">✎</button><button class="row-action" data-action="delete-food" data-id="${escapeHtml(food.id)}" aria-label="Delete ${escapeHtml(food.name)}">×</button></td></tr>`).join("");
    return `${renderHeading("My foods", "Your personal food list. Nutrition values are recorded per serving unit.", "Add a food", "add-food")}<div class="filter-row"><label class="search-field"><span>⌕</span><input id="food-search" type="search" placeholder="Find a food..." value="${escapeHtml(searchTerm)}"></label><select class="filter-select" id="food-category" aria-label="Filter by category">${categories.map((category) => `<option ${category === categoryFilter ? "selected" : ""}>${escapeHtml(category)}</option>`).join("")}</select><span class="pill">${filtered.length} foods</span></div><section class="card list-card"><div class="table-wrap"><table class="data-table"><thead><tr><th>Food</th><th>Category</th><th>Calories / unit</th><th>Protein / unit</th><th>Carbs / unit</th><th>Fat / unit</th><th>Source</th><th></th></tr></thead><tbody>${rows || `<tr><td colspan="8"><div class="empty-state"><span class="empty-icon">🥑</span><h3>No foods found</h3><p>Try a different search or add your own food.</p></div></td></tr>`}</tbody></table></div></section><p class="field-hint" style="margin:12px 2px">Values are per ${escapeHtml(state.foods[0]?.unit || "serving")}. Estimated values are clearly marked; use package or trusted source information when available.</p>`;
  }

  function renderPantry() {
    const cards = state.pantry.map((entry) => {
      const food = getFood(entry.foodId);
      if (!food) return "";
      const low = entry.quantity <= entry.lowAt;
      const percent = Math.min(entry.quantity / Math.max(entry.lowAt * 3, 1) * 100, 100);
      const expiry = entry.expires ? `Best by ${dateLabel(entry.expires, { month: "short", day: "numeric" })}` : "No expiry date";
      return `<article class="card inventory-card"><div class="inventory-top"><span class="inventory-food"><span class="food-badge">${categoryEmojis[food.category] || "🥬"}</span><span><strong>${escapeHtml(food.name)}</strong><small>${escapeHtml(food.category)}</small></span></span><span class="pill ${low ? "warning" : ""}">${low ? "Running low" : "In stock"}</span></div><div class="inventory-quantity">${number(entry.quantity, 1)} <small>${escapeHtml(food.unit)}</small></div><div class="stock-track"><div class="stock-fill ${low ? "low" : ""}" style="width:${percent}%"></div></div><div class="inventory-foot"><span>${escapeHtml(expiry)}</span><span>Low at ${number(entry.lowAt)} ${escapeHtml(food.unit)}</span></div><div class="inventory-actions"><button class="button button-secondary" data-action="adjust-stock" data-id="${escapeHtml(entry.id)}" data-direction="subtract">− Use some</button><button class="button button-secondary" data-action="adjust-stock" data-id="${escapeHtml(entry.id)}" data-direction="add">＋ Restock</button><button class="button button-danger" data-action="delete-stock" data-id="${escapeHtml(entry.id)}">Remove</button></div></article>`;
    }).join("");
    return `${renderHeading("My pantry", "A helpful snapshot of what's in your kitchen. Stock is an estimate, so adjust it whenever life happens.", "Add pantry item", "add-stock")}<div class="notice" style="margin-bottom:15px"><strong>Good to know:</strong> Pantry quantities are only as accurate as the entries you make. Meal logging doesn't change stock automatically; update it here when you've used ingredients.</div>${cards ? `<div class="inventory-grid">${cards}</div>` : `<section class="card"><div class="empty-state"><span class="empty-icon">🧺</span><h3>Your pantry is ready for a first item</h3><p>Add one of your saved foods and an estimate of what's on hand.</p><button class="button button-primary button-small" data-action="add-stock">Add pantry item</button></div></section>`}`;
  }

  function renderRecipes() {
    const cards = state.recipes.map((recipe) => {
      const nutrition = recipeNutrition(recipe);
      const names = recipe.items.map((item) => getFood(item.foodId)?.name).filter(Boolean);
      const hasIngredients = recipe.items.every((item) => state.pantry.some((stock) => stock.foodId === item.foodId && stock.quantity >= item.quantity / recipe.servings));
      return `<article class="card recipe-card"><div class="recipe-art">🥗</div><h3>${escapeHtml(recipe.name)}</h3><p>${escapeHtml(recipe.description || "A little inspiration from your kitchen.")}</p><div class="recipe-meta"><span>◷ ${number(recipe.prepMinutes)} min</span><span>🍽 ${number(recipe.servings)} ${recipe.servings === 1 ? "serving" : "servings"}</span></div><p class="recipe-ingredients">${names.map(escapeHtml).join(" · ")}</p><div class="recipe-footer"><strong>${number(nutrition.calories)} kcal / serving</strong><button class="button button-secondary button-small" data-action="log-recipe" data-id="${escapeHtml(recipe.id)}">Log meal</button></div><div class="inventory-foot"><span>${hasIngredients ? "✓ Ingredients on hand" : "Some ingredients may be missing"}</span><button class="row-action" data-action="delete-recipe" data-id="${escapeHtml(recipe.id)}" aria-label="Delete ${escapeHtml(recipe.name)}">×</button></div></article>`;
    }).join("");
    return `${renderHeading("Recipes", "Easy ideas built from foods you already know. Save a recipe, then log it when you enjoy it.", "Create a recipe", "add-recipe")}<div class="notice" style="margin-bottom:15px"><strong>Recipe nutrition is an estimate</strong> calculated from your food list and serving sizes. It is not a substitute for package labels or professional advice.</div>${cards ? `<div class="recipe-grid">${cards}</div>` : `<section class="card"><div class="empty-state"><span class="empty-icon">📖</span><h3>Your recipe collection starts here</h3><p>Build a simple recipe from your saved foods.</p><button class="button button-primary button-small" data-action="add-recipe">Create a recipe</button></div></section>`}`;
  }

  function foodOptions(selected = "") {
    return state.foods.map((food) => `<option value="${escapeHtml(food.id)}" ${food.id === selected ? "selected" : ""}>${escapeHtml(food.name)} · ${escapeHtml(food.unit)}</option>`).join("");
  }

  function modal(title, description, body, submitText, formName) {
    modalRoot.innerHTML = `<div class="modal-backdrop"><section class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><header class="modal-header"><div><h2 id="modal-title">${title}</h2><p>${description}</p></div><button class="modal-close" type="button" data-action="close-modal" aria-label="Close dialog">×</button></header><form id="${formName}"><div class="modal-body">${body}</div><footer class="modal-actions"><button class="button button-secondary" type="button" data-action="close-modal">Cancel</button><button class="button button-primary" type="submit">${submitText}</button></footer></form></section></div>`;
    modalRoot.querySelector("input, select, textarea")?.focus();
  }

  function openGoalsModal() {
    const goals = state.goals;
    modal("Your daily goals", "Choose targets that feel useful for you. You can change them at any time.", `<div class="notice"><strong>Personal, not prescriptive.</strong> These goals are for your own tracking and are not medical advice.</div><div class="nutrient-grid">${goalField("Calories", "calories", goals.calories, "kcal")}${goalField("Protein", "protein", goals.protein, "g")}${goalField("Carbohydrates", "carbs", goals.carbs, "g")}${goalField("Fat", "fat", goals.fat, "g")}</div>`, "Save goals", "goals-form");
  }

  function goalField(label, name, value, unit) {
    return `<div class="field"><label for="goal-${name}">${label} (${unit})</label><input class="field-input" id="goal-${name}" name="${name}" type="number" min="1" max="10000" step="1" value="${number(value)}" required></div>`;
  }

  function openFoodModal(food = null) {
    const categoryOptions = ["Produce", "Fruit", "Protein", "Dairy", "Grains", "Other"].map((category) => `<option ${food?.category === category ? "selected" : ""}>${category}</option>`).join("");
    const confidenceOptions = ["Estimated", "Package label", "Verified source"].map((confidence) => `<option ${food?.confidence === confidence ? "selected" : ""}>${confidence}</option>`).join("");
    const body = `<input type="hidden" name="foodId" value="${escapeHtml(food?.id || "")}"><div class="form-grid"><div class="field"><label for="food-name">Food name</label><input class="field-input" id="food-name" name="name" maxlength="70" required placeholder="e.g. Rolled oats" value="${escapeHtml(food?.name || "")}"></div><div class="field"><label for="food-category-select">Category</label><select class="field-select" id="food-category-select" name="category">${categoryOptions}</select></div><div class="field"><label for="food-unit">Serving unit</label><input class="field-input" id="food-unit" name="unit" maxlength="16" required placeholder="g, ml, piece" value="${escapeHtml(food?.unit || "")}"></div><div class="field"><label for="food-confidence">Nutrition source</label><select class="field-select" id="food-confidence" name="confidence">${confidenceOptions}</select></div></div><div class="field"><label>Nutrients per serving unit</label><div class="nutrient-grid">${nutrientField("Calories", "calories", "kcal", food?.calories)}${nutrientField("Protein", "protein", "g", food?.protein)}${nutrientField("Carbohydrates", "carbs", "g", food?.carbs)}${nutrientField("Fat", "fat", "g", food?.fat)}</div><span class="field-hint">Use per-gram values for gram units, or per-piece values for a piece unit.</span></div>`;
    modal(food ? "Edit a food" : "Add a food", "Add the nutrition listed for one serving unit. You can edit or remove this food later.", body, "Save food", "food-form");
  }

  function nutrientField(label, name, unit, value = 0) {
    return `<div class="field"><label for="nutrient-${name}">${label} (${unit})</label><input class="field-input" id="nutrient-${name}" name="${name}" type="number" min="0" max="10000" step="0.001" required value="${escapeHtml(value ?? 0)}"></div>`;
  }

  function openMealModal(recipe) {
    if (!state.foods.length) return showToast("Add a food before logging a meal.");
    const ingredients = recipe ? recipe.items.map((item) => ingredientEntry(item.foodId, item.quantity / recipe.servings)).join("") : ingredientEntry();
    modal("Log a meal", recipe ? `Adding ${escapeHtml(recipe.name)} to your meal journal.` : "Add the foods you enjoyed. Nutrition totals are calculated from your food list.", `<div class="form-grid"><div class="field"><label for="meal-type">Meal</label><select class="field-select" id="meal-type" name="type"><option>Breakfast</option><option>Lunch</option><option>Dinner</option><option>Snack</option><option>Other</option></select></div><div class="field"><label for="meal-date">Date</label><input class="field-input" id="meal-date" name="date" type="date" value="${escapeHtml(selectedDate)}" max="${today}" required></div><div class="field"><label for="meal-time">Time</label><input class="field-input" id="meal-time" name="time" type="time" value="${new Date().toTimeString().slice(0, 5)}" required></div><div class="field"><label for="meal-notes">Notes (optional)</label><input class="field-input" id="meal-notes" name="notes" maxlength="160" placeholder="Anything you'd like to remember?"></div></div><div class="field"><label>What did you have?</label><div id="meal-ingredients">${ingredients}</div><button class="button button-secondary button-small" type="button" data-action="add-meal-row" style="margin-top:8px">＋ Add another food</button></div>`, "Save meal", "meal-form");
  }

  function ingredientEntry(selected = "", quantity = "") {
    return `<div class="ingredient-entry" data-ingredient><div class="field"><label>Food</label><select class="field-select" name="foodId" required>${foodOptions(selected)}</select></div><div class="field"><label>Quantity</label><input class="field-input" name="quantity" type="number" min="0.001" max="100000" step="0.001" value="${quantity}" required placeholder="e.g. 1"></div><button class="remove-entry" type="button" data-action="remove-row" aria-label="Remove food">×</button></div>`;
  }

  function openStockModal() {
    const availableFoods = state.foods.filter((food) => !state.pantry.some((entry) => entry.foodId === food.id));
    if (!availableFoods.length) return showToast("Every saved food is already in your pantry. Add another food first.");
    modal("Add to your pantry", "Add an estimate of the quantity you have. You can correct it at any time.", `<div class="form-grid"><div class="field full"><label for="stock-food">Food</label><select class="field-select" id="stock-food" name="foodId" required>${availableFoods.map((food) => `<option value="${escapeHtml(food.id)}">${escapeHtml(food.name)} · ${escapeHtml(food.unit)}</option>`).join("")}</select></div><div class="field"><label for="stock-quantity">Quantity on hand</label><input class="field-input" id="stock-quantity" name="quantity" type="number" min="0.001" max="100000" step="0.001" required placeholder="e.g. 500"></div><div class="field"><label for="stock-low">Low-stock reminder at</label><input class="field-input" id="stock-low" name="lowAt" type="number" min="0" max="100000" step="0.001" value="1" required></div><div class="field"><label for="stock-expiry">Best by (optional)</label><input class="field-input" id="stock-expiry" name="expires" type="date"></div></div>`, "Add item", "stock-form");
  }

  function openRecipeModal() {
    if (!state.foods.length) return showToast("Add foods before creating a recipe.");
    modal("Create a recipe", "Save an idea you enjoy, then log it when it's on the menu.", `<div class="form-grid"><div class="field full"><label for="recipe-name">Recipe name</label><input class="field-input" id="recipe-name" name="name" maxlength="70" required placeholder="e.g. Weeknight grain bowl"></div><div class="field full"><label for="recipe-description">A few words about it (optional)</label><textarea class="field-textarea" id="recipe-description" name="description" maxlength="240" placeholder="What makes it a favorite?"></textarea></div><div class="field"><label for="recipe-prep">Prep time (minutes)</label><input class="field-input" id="recipe-prep" name="prepMinutes" type="number" min="1" max="1440" step="1" value="20" required></div><div class="field"><label for="recipe-servings">Servings</label><input class="field-input" id="recipe-servings" name="servings" type="number" min="1" max="100" step="1" value="1" required></div></div><div class="field"><label>Ingredients and quantities</label><div id="recipe-ingredients">${ingredientEntry()}</div><button class="button button-secondary button-small" type="button" data-action="add-recipe-row" style="margin-top:8px">＋ Add ingredient</button></div>`, "Save recipe", "recipe-form");
  }

  function saveMeal(form) {
    const data = new FormData(form);
    const date = data.get("date");
    if (date > today) return showToast("Future meals can't be logged as eaten.");
    const items = [...form.querySelectorAll("[data-ingredient]")].map((row) => {
      const food = getFood(row.querySelector('[name="foodId"]').value);
      const quantity = Number(row.querySelector('[name="quantity"]').value);
      if (!food || !Number.isFinite(quantity) || quantity <= 0) throw new Error("Choose a food and enter a positive quantity for every item.");
      return { foodId: food.id, name: food.name, unit: food.unit, quantity, calories: food.calories, protein: food.protein, carbs: food.carbs, fat: food.fat };
    });
    if (!items.length) return showToast("A meal needs at least one food.");
    if (backendMode) {
      apiRequest("/meals", {
        method: "POST",
        body: JSON.stringify({ type: data.get("type"), date, time: data.get("time"), notes: String(data.get("notes") || "").trim(), items: items.map(({ foodId, quantity }) => ({ foodId, quantity })) })
      }).then((meal) => {
        state.meals.push(normalizeMeal(meal));
        if (!persist()) return;
        selectedDate = date;
        closeModal();
        render();
        refreshRecommendations();
        showToast("Meal saved to your journal.");
      }).catch(showApiFailure);
      return;
    }
    state.meals.push({ id: createId(), type: data.get("type"), date, time: data.get("time"), notes: String(data.get("notes") || "").trim(), items });
    if (!persist()) { state.meals.pop(); return; }
    selectedDate = date;
    closeModal();
    render();
    showToast("Meal saved to your journal.");
  }

  function saveFood(form) {
    const data = new FormData(form);
    const name = String(data.get("name")).trim();
    const id = String(data.get("foodId") || "");
    if (state.foods.some((food) => food.id !== id && food.name.toLocaleLowerCase() === name.toLocaleLowerCase())) return showToast("A food with that name already exists.");
    const food = { id: id || createId(), name, category: data.get("category"), unit: String(data.get("unit")).trim(), confidence: data.get("confidence"), calories: Number(data.get("calories")), protein: Number(data.get("protein")), carbs: Number(data.get("carbs")), fat: Number(data.get("fat")) };
    if (backendMode) {
      const { id: ignoredId, ...request } = food;
      apiRequest(id ? `/foods/${encodeURIComponent(id)}` : "/foods", {
        method: id ? "PUT" : "POST", body: JSON.stringify(request)
      }).then((savedFood) => {
        state.foods = id ? state.foods.map((item) => item.id === id ? savedFood : item) : [...state.foods, savedFood];
        if (!persist()) return;
        closeModal();
        render();
        refreshRecommendations();
        showToast(id ? "Food details updated." : "Food added to your library.");
      }).catch(showApiFailure);
      return;
    }
    const previousFoods = state.foods;
    state.foods = id ? state.foods.map((savedFood) => savedFood.id === id ? food : savedFood) : [...state.foods, food];
    if (!persist()) { state.foods = previousFoods; return; }
    closeModal();
    render();
    showToast(id ? "Food details updated." : "Food added to your library.");
  }

  function saveStock(form) {
    const data = new FormData(form);
    const request = { foodId: data.get("foodId"), quantity: Number(data.get("quantity")), lowAt: Number(data.get("lowAt")), expires: data.get("expires") || null };
    if (backendMode) {
      apiRequest("/pantry", { method: "POST", body: JSON.stringify(request) }).then((item) => {
        state.pantry.push({ id: item.id, foodId: item.foodId, quantity: item.quantity, lowAt: item.lowAt, expires: item.expires || "" });
        if (!persist()) return;
        closeModal();
        render();
        refreshRecommendations();
        showToast("Pantry item added.");
      }).catch(showApiFailure);
      return;
    }
    state.pantry.push({ id: createId(), ...request, expires: request.expires || "" });
    if (!persist()) { state.pantry.pop(); return; }
    closeModal();
    render();
    showToast("Pantry item added.");
  }

  function saveRecipe(form) {
    const data = new FormData(form);
    const items = [...form.querySelectorAll("[data-ingredient]")].map((row) => {
      const food = getFood(row.querySelector('[name="foodId"]').value);
      const quantity = Number(row.querySelector('[name="quantity"]').value);
      if (!food || !Number.isFinite(quantity) || quantity <= 0) throw new Error("Choose a food and enter a positive quantity for every ingredient.");
      return { foodId: food.id, quantity };
    });
    if (!items.length) return showToast("A recipe needs at least one ingredient.");
    if (backendMode) {
      const request = { name: String(data.get("name")).trim(), description: String(data.get("description") || "").trim(), prepMinutes: Number(data.get("prepMinutes")), servings: Number(data.get("servings")), items };
      apiRequest("/recipes", { method: "POST", body: JSON.stringify(request) }).then((recipe) => {
        state.recipes.push(normalizeRecipe(recipe));
        if (!persist()) return;
        closeModal();
        render();
        refreshRecommendations();
        showToast("Recipe saved.");
      }).catch(showApiFailure);
      return;
    }
    state.recipes.push({ id: createId(), name: String(data.get("name")).trim(), description: String(data.get("description") || "").trim(), prepMinutes: Number(data.get("prepMinutes")), servings: Number(data.get("servings")), items });
    if (!persist()) { state.recipes.pop(); return; }
    closeModal();
    render();
    showToast("Recipe saved.");
  }

  function saveGoals(form) {
    const data = new FormData(form);
    const request = { calories: Number(data.get("calories")), protein: Number(data.get("protein")), carbs: Number(data.get("carbs")), fat: Number(data.get("fat")) };
    if (backendMode) {
      apiRequest("/goals", { method: "PUT", body: JSON.stringify(request) }).then((goals) => {
        state.goals = goals;
        if (!persist()) return;
        closeModal();
        render();
        refreshRecommendations();
        showToast("Your daily goals have been updated.");
      }).catch(showApiFailure);
      return;
    }
    const previousGoals = state.goals;
    state.goals = request;
    if (!persist()) { state.goals = previousGoals; return; }
    closeModal();
    render();
    showToast("Your daily goals have been updated.");
  }

  function closeModal() {
    modalRoot.innerHTML = "";
  }

  function deleteMeal(id) {
    if (backendMode) {
      apiRequest(`/meals/${encodeURIComponent(id)}`, { method: "DELETE" }).then(() => {
        state.meals = state.meals.filter((meal) => meal.id !== id);
        if (!persist()) return;
        render();
        refreshRecommendations();
        showToast("Meal removed. Daily totals have been updated.");
      }).catch(showApiFailure);
      return;
    }
    const previousMeals = state.meals;
    state.meals = state.meals.filter((meal) => meal.id !== id);
    if (!persist()) { state.meals = previousMeals; return; }
    render();
    showToast("Meal removed. Daily totals have been updated.");
  }

  function deleteFood(id) {
    const used = state.meals.some((meal) => meal.items.some((item) => item.foodId === id));
    if (used) return showToast("This food is part of your meal history and can't be removed.");
    if (!window.confirm("Remove this food from your library? Pantry items and recipes that use it will also be removed.")) return;
    if (backendMode) {
      apiRequest(`/foods/${encodeURIComponent(id)}`, { method: "DELETE" }).then(() => {
        state.foods = state.foods.filter((food) => food.id !== id);
        state.pantry = state.pantry.filter((entry) => entry.foodId !== id);
        state.recipes = state.recipes.map((recipe) => ({ ...recipe, items: recipe.items.filter((item) => item.foodId !== id) })).filter((recipe) => recipe.items.length);
        if (!persist()) return;
        render();
        refreshRecommendations();
        showToast("Food removed.");
      }).catch(showApiFailure);
      return;
    }
    const previousState = { foods: state.foods, pantry: state.pantry, recipes: state.recipes };
    state.foods = state.foods.filter((food) => food.id !== id);
    state.pantry = state.pantry.filter((entry) => entry.foodId !== id);
    state.recipes = state.recipes.map((recipe) => ({ ...recipe, items: recipe.items.filter((item) => item.foodId !== id) })).filter((recipe) => recipe.items.length);
    if (!persist()) { Object.assign(state, previousState); return; }
    render();
    showToast("Food removed.");
  }

  function adjustStock(id, direction) {
    const entry = state.pantry.find((stock) => stock.id === id);
    if (!entry) return;
    const food = getFood(entry.foodId);
    const amount = window.prompt(`${direction === "add" ? "Restock" : "Use"} ${food?.name || "item"} (${food?.unit || "units"}):`, String(Math.min(entry.quantity, 1)));
    if (amount === null) return;
    const quantity = Number(amount);
    if (!Number.isFinite(quantity) || quantity <= 0) return showToast("Enter a quantity greater than zero.");
    if (direction === "subtract" && quantity > entry.quantity) return showToast("That is more than the quantity currently in your pantry.");
    if (backendMode) {
      apiRequest(`/pantry/${encodeURIComponent(id)}/adjust`, {
        method: "PATCH", body: JSON.stringify({ quantity, add: direction === "add" })
      }).then((updated) => {
        Object.assign(entry, { quantity: updated.quantity, lowAt: updated.lowAt, expires: updated.expires || "" });
        if (!persist()) return;
        render();
        refreshRecommendations();
        showToast(direction === "add" ? "Pantry quantity updated." : "Quantity used. Pantry updated.");
      }).catch(showApiFailure);
      return;
    }
    const previousQuantity = entry.quantity;
    entry.quantity += direction === "add" ? quantity : -quantity;
    if (!persist()) { entry.quantity = previousQuantity; return; }
    render();
    showToast(direction === "add" ? "Pantry quantity updated." : "Quantity used. Pantry updated.");
  }

  function deleteStock(id) {
    if (!window.confirm("Remove this item from your pantry?")) return;
    if (backendMode) {
      apiRequest(`/pantry/${encodeURIComponent(id)}`, { method: "DELETE" }).then(() => {
        state.pantry = state.pantry.filter((item) => item.id !== id);
        if (!persist()) return;
        render();
        refreshRecommendations();
        showToast("Pantry item removed.");
      }).catch(showApiFailure);
      return;
    }
    const previousPantry = state.pantry;
    state.pantry = state.pantry.filter((item) => item.id !== id);
    if (!persist()) { state.pantry = previousPantry; return; }
    render();
    showToast("Pantry item removed.");
  }

  function logRecipe(id) {
    const recipe = state.recipes.find((item) => item.id === id);
    if (recipe) openMealModal(recipe);
  }

  function exportData() {
    if (storageReadOnly && !unreadableData) {
      showToast("No saved data could be read from this browser.");
      return;
    }
    const recoveryExport = storageReadOnly;
    const content = recoveryExport ? unreadableData : JSON.stringify({ exportedAt: new Date().toISOString(), ...state }, null, 2);
    const blob = new Blob([content], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = recoveryExport ? `nutrikitchen-recovery-${today}.json` : `nutrikitchen-${today}.json`;
    link.click();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    showToast(recoveryExport ? "Original saved data downloaded for recovery; it may need repair." : "Your kitchen data has been exported.");
  }

  function validateImportedState(value) {
    const finiteNonNegative = (number) => Number.isFinite(number) && number >= 0;
    const uniqueIds = (entries) => new Set(entries.map((entry) => entry.id)).size === entries.length;
    const ids = new Set(Array.isArray(value?.foods) ? value.foods.map((food) => food.id) : []);
    const validNutrients = (item) => ["calories", "protein", "carbs", "fat"].every((key) => finiteNonNegative(item[key]));
    if (!value || !value.goals || !["calories", "protein", "carbs", "fat"].every((key) => Number.isFinite(value.goals[key]) && value.goals[key] > 0)) return false;
    if (!Array.isArray(value.foods) || !uniqueIds(value.foods) || !value.foods.every((food) => typeof food.id === "string" && typeof food.name === "string" && typeof food.category === "string" && typeof food.unit === "string" && typeof food.confidence === "string" && validNutrients(food))) return false;
    if (!Array.isArray(value.meals) || !uniqueIds(value.meals) || !value.meals.every((meal) => typeof meal.id === "string" && typeof meal.type === "string" && /^\d{4}-\d{2}-\d{2}$/.test(meal.date) && /^\d{2}:\d{2}$/.test(meal.time) && typeof meal.notes === "string" && Array.isArray(meal.items) && meal.items.length > 0 && meal.items.every((item) => typeof item.foodId === "string" && typeof item.name === "string" && typeof item.unit === "string" && Number.isFinite(item.quantity) && item.quantity > 0 && validNutrients(item)))) return false;
    if (!Array.isArray(value.pantry) || !uniqueIds(value.pantry) || !value.pantry.every((entry) => typeof entry.id === "string" && ids.has(entry.foodId) && finiteNonNegative(entry.quantity) && finiteNonNegative(entry.lowAt) && typeof entry.expires === "string")) return false;
    if (!Array.isArray(value.recipes) || !uniqueIds(value.recipes) || !value.recipes.every((recipe) => typeof recipe.id === "string" && typeof recipe.name === "string" && typeof recipe.description === "string" && Number.isFinite(recipe.prepMinutes) && recipe.prepMinutes > 0 && Number.isFinite(recipe.servings) && recipe.servings > 0 && Array.isArray(recipe.items) && recipe.items.length > 0 && recipe.items.every((item) => ids.has(item.foodId) && Number.isFinite(item.quantity) && item.quantity > 0))) return false;
    return true;
  }

  async function importData(file) {
    if (!file) return;
    try {
      const imported = JSON.parse(await file.text());
      if (!validateImportedState(imported)) throw new Error("The backup is missing required NutriKitchen data or contains invalid values.");
      if (!window.confirm(backendMode
        ? "Import this backup and replace the data stored on your NutriKitchen server?"
        : "Import this backup and replace the data saved in this browser?")) return;
      if (backendMode) {
        await apiRequest("/data/import", { method: "POST", body: JSON.stringify(localDataForImport(imported)) });
        await refreshBackendState();
        storageWarning = "";
        storageWarningShown = false;
        unreadableData = "";
        render();
        showToast("Backup imported to your NutriKitchen server.");
        return;
      }
      const previousState = state;
      const previousReadOnly = storageReadOnly;
      state = { goals: imported.goals, foods: imported.foods, meals: imported.meals, pantry: imported.pantry, recipes: imported.recipes };
      storageReadOnly = false;
      if (!persist()) {
        state = previousState;
        storageReadOnly = previousReadOnly;
        return;
      }
      storageWarning = "";
      storageWarningShown = false;
      unreadableData = "";
      render();
      showToast("Backup imported.");
    } catch (error) {
      console.error("Could not import NutriKitchen backup.", error);
      showToast(error.message || "Could not read that backup file.");
    } finally {
      document.querySelector("#import-file").value = "";
    }
  }

  function resetLocalData() {
    if (!window.confirm(backendMode
      ? "Replace all data in your NutriKitchen server with the example kitchen? Export a backup first if you want to keep it."
      : "Clear saved NutriKitchen data from this browser and start with the example kitchen? Export a backup first if you want to keep it.")) return;
    if (backendMode) {
      apiRequest("/data/import", { method: "POST", body: JSON.stringify(localDataForImport(cloneData(starterData))) })
        .then(refreshBackendState).then(() => {
          render();
          showToast("Server data reset to the example kitchen.");
        }).catch(showApiFailure);
      return;
    }
    try {
      localStorage.removeItem(STORAGE_KEY);
      state = cloneData(starterData);
      storageReadOnly = false;
      storageWarning = "";
      storageWarningShown = false;
      unreadableData = "";
      render();
      showToast("Local data cleared. Your example kitchen is ready.");
    } catch (error) {
      console.error("Could not reset local NutriKitchen data.", error);
      showToast("Could not clear browser storage. Check your browser's site-data settings.");
    }
  }

  function handleAction(action, target) {
    if (action === "log-meal") openMealModal();
    if (action === "export-data") exportData();
    if (action === "import-data") document.querySelector("#import-file").click();
    if (action === "add-food") openFoodModal();
    if (action === "edit-food") openFoodModal(getFood(target.dataset.id));
    if (action === "add-stock") openStockModal();
    if (action === "add-recipe") openRecipeModal();
    if (action === "edit-goals") openGoalsModal();
    if (action === "reset-data") resetLocalData();
    if (action === "close-modal") closeModal();
    if (action === "add-meal-row" || action === "add-recipe-row") {
      const container = modalRoot.querySelector(action === "add-meal-row" ? "#meal-ingredients" : "#recipe-ingredients");
      container.insertAdjacentHTML("beforeend", ingredientEntry());
    }
    if (action === "remove-row") {
      const rows = target.closest(".modal").querySelectorAll("[data-ingredient]");
      if (rows.length === 1) return showToast("Keep at least one food or ingredient.");
      target.closest("[data-ingredient]").remove();
    }
    if (action === "delete-meal" && window.confirm("Remove this meal from your journal? Daily totals will be recalculated.")) deleteMeal(target.dataset.id);
    if (action === "delete-food") deleteFood(target.dataset.id);
    if (action === "delete-recipe" && window.confirm("Remove this recipe?")) {
      if (backendMode) {
        apiRequest(`/recipes/${encodeURIComponent(target.dataset.id)}`, { method: "DELETE" }).then(() => {
          state.recipes = state.recipes.filter((recipe) => recipe.id !== target.dataset.id);
          if (!persist()) return;
          render();
          refreshRecommendations();
          showToast("Recipe removed.");
        }).catch(showApiFailure);
        return;
      }
      const previousRecipes = state.recipes;
      state.recipes = state.recipes.filter((recipe) => recipe.id !== target.dataset.id);
      if (persist()) { render(); showToast("Recipe removed."); }
      else state.recipes = previousRecipes;
    }
    if (action === "adjust-stock") adjustStock(target.dataset.id, target.dataset.direction);
    if (action === "delete-stock") deleteStock(target.dataset.id);
    if (action === "log-recipe") logRecipe(target.dataset.id);
  }

  document.addEventListener("click", (event) => {
    const target = event.target.closest("[data-action], [data-navigate]");
    if (!target) return;
    if (target.dataset.navigate) {
      currentPage = target.dataset.navigate;
      location.hash = currentPage;
      render();
      return;
    }
    handleAction(target.dataset.action, target);
  });

  document.addEventListener("submit", (event) => {
    if (event.target.id === "meal-form" || event.target.id === "recipe-form") {
      event.preventDefault();
      try {
        if (event.target.id === "meal-form") saveMeal(event.target);
        else saveRecipe(event.target);
      } catch (error) {
        console.error("Could not save the form.", error);
        showToast(error.message || "Could not save. Check the form and try again.");
      }
    }
    if (event.target.id === "food-form") { event.preventDefault(); saveFood(event.target); }
    if (event.target.id === "stock-form") { event.preventDefault(); saveStock(event.target); }
    if (event.target.id === "goals-form") { event.preventDefault(); saveGoals(event.target); }
  });

  document.addEventListener("change", (event) => {
    if (event.target.id === "dashboard-date" || event.target.id === "journal-date") {
      const requestedDate = event.target.value || today;
      selectedDate = requestedDate > today ? today : requestedDate;
      render();
      refreshRecommendations();
    }
    if (event.target.id === "food-category") {
      categoryFilter = event.target.value;
      render();
    }
  });

  document.addEventListener("input", (event) => {
    if (event.target.id === "food-search") {
      const position = event.target.selectionStart;
      searchTerm = event.target.value;
      render();
      const input = document.querySelector("#food-search");
      input.focus();
      input.setSelectionRange(position, position);
    }
  });

  document.querySelectorAll(".main-nav a").forEach((link) => link.addEventListener("click", () => { currentPage = link.dataset.page; }));
  window.addEventListener("hashchange", () => {
    currentPage = location.hash.slice(1) || "dashboard";
    if (!pageTitles[currentPage]) currentPage = "dashboard";
    render();
  });
  document.querySelector("#goals-button").addEventListener("click", openGoalsModal);
  document.querySelector("#export-button").addEventListener("click", exportData);
  document.querySelector("#import-button").addEventListener("click", () => document.querySelector("#import-file").click());
  document.querySelector("#import-file").addEventListener("change", (event) => importData(event.target.files[0]));
  document.querySelector("#menu-button").addEventListener("click", (event) => {
    const sidebar = document.querySelector("#sidebar");
    const isOpen = sidebar.classList.toggle("open");
    event.currentTarget.setAttribute("aria-expanded", String(isOpen));
  });

  render();
  connectBackend();
})();
