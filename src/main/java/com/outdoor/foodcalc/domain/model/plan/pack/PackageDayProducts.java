package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents part of a package that belongs to a specific day.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PackageDayProducts {
    private String packageName;
    private int dayIndex;
    private double weight; // вага, яку використано в цей день
}
