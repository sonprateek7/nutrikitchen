package com.example.nutrikitchen.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pantry_items")
public class PantryItem {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_item_id", nullable = false, unique = true)
    private FoodItem food;

    @DecimalMin("0")
    @DecimalMax("100000")
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @DecimalMin("0")
    @DecimalMax("100000")
    @Column(name = "low_at", nullable = false, precision = 12, scale = 3)
    private BigDecimal lowAt;

    private LocalDate expires;

    @Version
    private long version;

    protected PantryItem() {
    }

    public PantryItem(UUID id, FoodItem food, BigDecimal quantity, BigDecimal lowAt, LocalDate expires) {
        this.id = id;
        this.food = food;
        this.quantity = quantity;
        this.lowAt = lowAt;
        this.expires = expires;
    }

    public void adjust(BigDecimal delta) {
        quantity = quantity.add(delta);
    }

    public void update(BigDecimal quantity, BigDecimal lowAt, LocalDate expires) {
        this.quantity = quantity;
        this.lowAt = lowAt;
        this.expires = expires;
    }

    public UUID getId() { return id; }
    public FoodItem getFood() { return food; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLowAt() { return lowAt; }
    public LocalDate getExpires() { return expires; }
}
