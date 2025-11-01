package com.outdoor.foodcalc.domain.model.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Simplified Hiker entity for mobile version.
 * Represents a trip participant with name and load coefficient.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class Hiker {

    @EqualsAndHashCode.Include
    private final long id;
    private String name;
    private String description;
    private double weightCoefficient;
}
