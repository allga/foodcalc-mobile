package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a package containing products, split by days.
 * Example: тушонка (110, 100, 90, 0) means day1=110g, day2=100g, etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PackageWithProducts {

    private String name;                      // назва пакунку (тушонка, каша і т.д.)
    private Map<Integer, Double> dayWeights;  // день → вага, у грамах

    public double getTotalWeight() {
        return dayWeights == null ? 0.0 : dayWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
