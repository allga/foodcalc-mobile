package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.*;

/**
 * Represents part of a package that belongs to a specific day.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PackageDayProducts {

    @EqualsAndHashCode.Include
    private String packageName;
    private int dayIndex;
    private double weight; // вага, яку використано в цей день
}
