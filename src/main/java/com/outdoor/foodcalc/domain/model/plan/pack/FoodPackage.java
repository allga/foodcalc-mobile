package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simplified FoodPackage class used for compatibility.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class FoodPackage {

    @EqualsAndHashCode.Include
    private String name;
    private double volumeCoefficient;
    private double additionalWeight;
    private double fullWeight;

    @Builder.Default
    private Map<LocalDate, Double> dayWeights = new LinkedHashMap<>();

    public double getWeightForDay(LocalDate date) {
        return dayWeights.getOrDefault(date, 0.0);
    }

    public double getTotalWeight() {
        return dayWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }

}
