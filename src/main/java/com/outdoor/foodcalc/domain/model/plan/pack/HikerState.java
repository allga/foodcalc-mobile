package com.outdoor.foodcalc.domain.model.plan.pack;

import com.outdoor.foodcalc.domain.model.plan.Hiker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tracks the assigned weight for each hiker by day.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class HikerState {

    private Hiker hiker;
    private Map<LocalDate, Double> loadByDay = new LinkedHashMap<>();

    public void addLoad(LocalDate date, double weight) {
        loadByDay.merge(date, weight, Double::sum);
    }

    public double getTotalLoad() {
        return loadByDay.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getLoadForDay(LocalDate date) {
        return loadByDay.getOrDefault(date, 0.0);
    }
}
