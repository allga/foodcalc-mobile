package com.outdoor.foodcalc.domain.model.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified FoodPlan entity for mobile version.
 * Represents the food plan structure used in distribution algorithm.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class FoodPlan {

    @EqualsAndHashCode.Include
    private final long id;
    private String name;
    private String description;

    @Builder.Default
    private List<PlanDay> days = new ArrayList<>();

    @Builder.Default
    private List<Hiker> members = new ArrayList<>();

    // convenience constructor
    public FoodPlan() {
        this.id = 0;
        this.name = "";
        this.description = "";
        this.days = new ArrayList<>();
        this.members = new ArrayList<>();
    }
}
