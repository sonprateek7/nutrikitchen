package com.example.nutrikitchen;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:nutrikitchen-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class NutrikitchenApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullKitchenApiWorkflowPersistsAndCalculatesNutrition() throws Exception {
        MvcResult foodResult = mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"API test oats","category":"Grains","unit":"serving",
                                 "calories":100,"protein":10,"carbs":20,"fat":2,"confidence":"Verified source"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("API test oats")))
                .andReturn();
        String foodId = com.jayway.jsonpath.JsonPath.read(foodResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"Breakfast","date":"%s","time":"08:15","notes":"",
                                 "items":[{"foodId":"%s","quantity":2}]}
                                """.formatted(LocalDate.now(), foodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totals.calories", is(200.0)))
                .andExpect(jsonPath("$.totals.protein", is(20.0)));

        mockMvc.perform(get("/api/daily-summary").param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.calories", is(200.0)))
                .andExpect(jsonPath("$.remaining.calories", is(1800.0)))
                .andExpect(jsonPath("$.meals.length()", is(1)));

        mockMvc.perform(put("/api/foods/{id}", foodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"API test oats","category":"Grains","unit":"serving",
                                 "calories":150,"protein":20,"carbs":30,"fat":4,"confidence":"Verified source"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories", is(150)));
        mockMvc.perform(get("/api/daily-summary").param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.calories", is(200.0)));

        mockMvc.perform(put("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"calories":1800,"protein":100,"carbs":200,"fat":60}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calories", is(1800)));

        MvcResult pantryResult = mockMvc.perform(post("/api/pantry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId":"%s","quantity":3,"lowAt":1,"expires":null}
                                """.formatted(foodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lowStock", is(false)))
                .andReturn();
        String pantryId = com.jayway.jsonpath.JsonPath.read(pantryResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/pantry/{id}", pantryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"foodId":"%s","quantity":3,"lowAt":1,"expires":null}
                                """.formatted(foodId)))
                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.quantity", is(3)));

        MvcResult recipeResult = mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"API test bowl","description":"","prepMinutes":10,"servings":1,
                                 "items":[{"foodId":"%s","quantity":1}]}
                                """.formatted(foodId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nutritionPerServing.calories", is(150.0)))
                .andExpect(jsonPath("$.ingredientsAvailable", is(true)))
                .andReturn();
        String recipeId = com.jayway.jsonpath.JsonPath.read(recipeResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/recommendations").param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].recipe.id", is(recipeId)));

        mockMvc.perform(patch("/api/pantry/{id}/adjust", pantryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":4,"add":false}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("insufficient_stock")));

        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"api TEST oats","category":"Grains","unit":"serving",
                                 "calories":100,"protein":10,"carbs":20,"fat":2,"confidence":"Estimated"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("duplicate_food_name")));

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"Snack","date":"2999-01-01","time":"08:15",
                                 "items":[{"foodId":"%s","quantity":1}]}
                                """.formatted(foodId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("future_meal")));

        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
        mockMvc.perform(delete("/api/foods/{id}", foodId))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/health").header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void rejectsInvalidFoodAndMealPayloadsWithUsefulErrors() throws Exception {
        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","category":"Grains","unit":"g",
                                 "calories":-1,"protein":0,"carbs":0,"fat":0,"confidence":"Estimated"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("validation_failed")))
                .andExpect(jsonPath("$.details.length()").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(post("/api/meals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"Lunch","date":"%s","time":"12:00",
                                 "items":[{"foodId":"00000000-0000-0000-0000-000000000001","quantity":0}]}
                                """.formatted(LocalDate.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("validation_failed")));
    }

    @Test
    void restoresBackupAtomicallyAndValidatesFoodReferences() throws Exception {
        mockMvc.perform(post("/api/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "exportedAt":"2026-10-31T12:00:00Z",
                                  "goals":{"calories":1900,"protein":110,"carbs":210,"fat":62},
                                  "foods":[{"id":"local-food","name":"Backup oats","category":"Grains",
                                    "unit":"g","calories":3,"protein":1,"carbs":2,"fat":0.5,
                                    "confidence":"Estimated"}],
                                  "meals":[{"id":"local-meal","type":"Breakfast","date":"%s","time":"08:15",
                                    "notes":"","items":[{"foodId":"local-food","name":"Oats, archived","unit":"g",
                                      "quantity":2,"calories":99,"protein":9,"carbs":8,"fat":1}]}],
                                  "pantry":[{"id":"local-pantry","foodId":"local-food","quantity":20,
                                    "lowAt":5,"expires":null}],
                                  "recipes":[{"id":"local-recipe","name":"Backup bowl","description":"",
                                    "prepMinutes":10,"servings":1,
                                    "items":[{"foodId":"local-food","quantity":10}]}]
                                }
                                """.formatted(LocalDate.now())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Backup oats")));

        mockMvc.perform(get("/api/daily-summary").param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.calories", is(198.0)))
                .andExpect(jsonPath("$.meals[0].items[0].name", is("Oats, archived")));

        mockMvc.perform(post("/api/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goals":{"calories":2000,"protein":120,"carbs":220,"fat":65},
                                 "foods":[],"meals":[],"pantry":[],
                                 "recipes":[{"id":"bad","name":"Bad recipe","description":"",
                                   "prepMinutes":10,"servings":1,
                                   "items":[{"foodId":"missing-food","quantity":1}]}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("invalid_food_reference")));

        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Backup oats")));
    }
}
