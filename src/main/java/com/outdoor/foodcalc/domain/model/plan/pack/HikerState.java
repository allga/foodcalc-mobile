package com.outdoor.foodcalc.domain.model.plan.pack;

import com.outdoor.foodcalc.domain.model.plan.Hiker;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks the assigned weight for each hiker by day.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HikerState {

    @EqualsAndHashCode.Include
    private Hiker hiker;

    /** Поточне навантаження туриста по днях */
    private Map<LocalDate, Double> loadByDay;

    /** Цільова вага (target) для кожного дня */
    private Map<LocalDate, Double> targetByDay;

    /** Усі пакунки, призначені туристу */
    private Set<PackageWithProducts> assignedPackages;

    /** Призначення по днях: день → множина пакунків */
    private Map<LocalDate, Set<PackageWithProducts>> assignedByDay;

    public HikerState(Hiker hiker) {
        this.hiker = hiker;
        this.loadByDay = new LinkedHashMap<>();
        this.targetByDay = new LinkedHashMap<>();
        this.assignedPackages = new LinkedHashSet<>();
        this.assignedByDay = new LinkedHashMap<>();
    }

    /** Додати пакунок туристу */
    private void assignPackage(LocalDate date, PackageWithProducts pack) {
        assignedPackages.add(pack);
        assignedByDay.computeIfAbsent(date, d -> new LinkedHashSet<>()).add(pack);
    }

    /** Додати пакунок туристу з урахуванням кількості учасників */
    public void addPackage(PackageWithProducts pack) {
        pack.getDayWeights().forEach((day, weight) -> {
            loadByDay.merge(day, weight, Double::sum);
            assignPackage(day, pack);
        });
    }

    /** Отримати навантаження на день */
    public double getLoadForDay(LocalDate date) {
        return loadByDay.getOrDefault(date, 0.0);
    }

    /** Загальна вага туриста для всіх днів */
    public double getTotalLoad() {
        return loadByDay.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Встановити цільову вагу для дня */
    public void setTargetByDay(LocalDate date, double target) {
        targetByDay.put(date, target);
    }

    /**  Скільки загальної ваги турист уже ніс (або несе) до певного дня включно */
    public double getTotalWeightUpTo(LocalDate date) {
        return loadByDay.entrySet().stream()
                .filter(e -> !e.getKey().isAfter(date))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    /** Клонування стану (для рекурсії BnB) */
    public HikerState cloneState() {
        HikerState clone = new HikerState(this.hiker);
        clone.loadByDay = new LinkedHashMap<>(this.loadByDay);
        clone.targetByDay = new LinkedHashMap<>(this.targetByDay);
        clone.assignedPackages = new LinkedHashSet<>(this.assignedPackages);

        clone.assignedByDay = new LinkedHashMap<>();
        this.assignedByDay.forEach((d, set) ->
                clone.assignedByDay.put(d, new LinkedHashSet<>(set))
        );
        return clone;
    }

    @Override
    public String toString() {
        String weights = loadByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + String.format("%.1f", e.getValue()))
                .collect(Collectors.joining(", "));

        String targets = targetByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + String.format("%.1f", e.getValue()))
                .collect(Collectors.joining(", "));

        String packagesInfo = assignedPackages.stream()
                .map(p -> p.getFoodPackage() != null ? p.getFoodPackage().getName() : "null")
                .collect(Collectors.joining(", "));

        return "[hiker=" + (hiker != null ? hiker.getName() : "null") +
                ", totalLoad=" + String.format("%.1f", getTotalLoad()) +
                ", weightByDay={" + weights + "}" +
                ", targetByDay={" + targets + "}" +
                ", assignedPackages=[" + packagesInfo + "]" +
                "]";
    }
}
