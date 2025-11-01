package com.outdoor.foodcalc.domain.model.plan;

import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified representation of a single day in FoodPlan.
 * Contains date and list of packages planned for that day.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class PlanDay {

    @EqualsAndHashCode.Include
    private long dayId;
    private LocalDate date;
    private String description;

    @Builder.Default
    private List<PackageWithProducts> packages = new ArrayList<>();

    public double getTotalWeight() {
        return packages.stream()
                .mapToDouble(p -> p.getDayWeights().values().stream().mapToDouble(Double::doubleValue).sum())
                .sum();
    }
}
