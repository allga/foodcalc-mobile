package com.outdoor.foodcalc.domain.model.plan.pack;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    @EqualsAndHashCode.Include
    private String name;
    private double fullWeight;

    /** Вага пакунка по днях (наприклад, тушонка (110, 100, 90, 0)) */
    @Builder.Default
    private Map<LocalDate, Double> dayWeights = new LinkedHashMap<>();

    /** Продукти в пакунку (спрощено) */
    @Builder.Default
    private List<String> products = new ArrayList<>();

    /** Отримати вагу для конкретного дня */
    public double getWeightForDay(LocalDate date) {
        return dayWeights.getOrDefault(date, 0.0);
    }

    /** Сумарна вага пакунка */
    public double getProductsWeight() {
        return dayWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Повернути "псевдо-обгортку" пакунка для логування */
    public FoodPackage getFoodPackage() {
        return FoodPackage.builder()
                .name(name)
                .fullWeight(fullWeight)
                .build();
    }

    /** Список днів використання пакунка */
    public List<LocalDate> getPackageDays() {
        return new ArrayList<>(dayWeights.keySet());
    }

}
