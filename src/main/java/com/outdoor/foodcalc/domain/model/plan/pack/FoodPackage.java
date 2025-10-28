package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified FoodPackage class used for compatibility.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class FoodPackage {

    private String name;
    private double totalWeight;
}
