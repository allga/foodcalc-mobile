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
    private final long id;
    private LocalDate date;   // ключове поле, бо алгоритм працює з розподілом по днях;

    @Builder.Default
    private List<PackageWithProducts> packages = new ArrayList<>();

    // optional helper (useful in imports)
    public PlanDay() {
        this.id = 0;
        this.date = LocalDate.now();
        this.packages = new ArrayList<>();
    }
}
