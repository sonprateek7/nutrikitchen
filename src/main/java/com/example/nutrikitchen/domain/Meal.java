package com.example.nutrikitchen.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    private UUID id;

    @NotBlank
    @Column(name = "meal_type", nullable = false, length = 20)
    private String type;

    @Column(name = "consumed_on", nullable = false)
    private LocalDate date;

    @Column(name = "consumed_at", nullable = false)
    private LocalTime time;

    @Size(max = 160)
    @Column(length = 160)
    private String notes;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id")
    private List<MealItem> items = new ArrayList<>();

    protected Meal() {
    }

    public Meal(UUID id, String type, LocalDate date, LocalTime time, String notes) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public void addItem(MealItem item) {
        items.add(item);
        item.setMeal(this);
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getNotes() { return notes; }
    public List<MealItem> getItems() { return items; }
}
