package com.outdoor.foodcalc.domain.model.plan;

import com.outdoor.foodcalc.domain.model.plan.pack.FoodPackage;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified FoodPlan entity for mobile version.
 * Represents the food plan structure used in distribution algorithm.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class FoodPlan {

    @EqualsAndHashCode.Include
    private long id;
    private String name;
    private String description;
    private String schemaVersion;

    @Builder.Default
    private List<PlanDay> days = new ArrayList<>();

    @Builder.Default
    private List<Hiker> members = new ArrayList<>();

    @Builder.Default
    private List<PackageWithProducts> packages = new ArrayList<>();

    public PlanDay findDayByDate(LocalDate date) {
        return days.stream().filter(d -> d.getDate().equals(date)).findFirst().orElse(null);
    }
}
