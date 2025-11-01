package com.outdoor.foodcalc.domain.model.plan;

import com.outdoor.foodcalc.domain.model.plan.pack.FoodPackage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
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
    private String schemaVersion;

    @Builder.Default
    private List<PlanDay> days = new ArrayList<>();

    @Builder.Default
    private List<Hiker> members = new ArrayList<>();

    @Builder.Default
    private List<FoodPackage> packages = new ArrayList<>();

    public PlanDay findDayByDate(LocalDate date) {
        return days.stream().filter(d -> d.getDate().equals(date)).findFirst().orElse(null);
    }
}
