package com.outdoor.foodcalc.domain.model.plan.pack;

import com.outdoor.foodcalc.domain.model.plan.Hiker;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Tracks the assigned weight for each hiker by day.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HikerState {

    @EqualsAndHashCode.Include
    private Hiker hiker;

    /** Поточне навантаження туриста по днях */
    @Builder.Default
    private final Map<LocalDate, Double> loadByDay = new LinkedHashMap<>();

    /** Цільова вага (target) для кожного дня */
    @Builder.Default
    private final Map<LocalDate, Double> targetByDay = new LinkedHashMap<>();

    /** Усі пакунки, призначені туристу */
    @Builder.Default
    private final Set<PackageWithProducts> assignedPackages = new LinkedHashSet<>();

    /** Призначення по днях: день → множина пакунків */
    @Builder.Default
    private final Map<LocalDate, Set<PackageWithProducts>> assignedByDay = new LinkedHashMap<>();


    /** Додати вагу */
    public void addLoad(LocalDate date, double weight) {
        loadByDay.merge(date, weight, Double::sum);
    }

    /** Додати пакунок туристу */
    public void assignPackage(LocalDate date, PackageWithProducts pack) {
        assignedPackages.add(pack);
        assignedByDay.computeIfAbsent(date, d -> new LinkedHashSet<>()).add(pack);
    }

    /** Отримати поточну вагу на день */
    public double getLoadForDay(LocalDate date) {
        return loadByDay.getOrDefault(date, 0.0);
    }

    /** Загальна вага туриста */
    public double getTotalLoad() {
        return loadByDay.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Встановити цільову вагу для дня */
    public void setTargetForDay(LocalDate date, double target) {
        targetByDay.put(date, target);
    }

    /** Отримати сумарну вагу до певного дня включно */
    public double getTotalWeightUpTo(LocalDate date) {
        return loadByDay.entrySet().stream()
                .filter(e -> !e.getKey().isAfter(date))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    /** Додати пакунок туристу з урахуванням кількості учасників */
    public void addPackage(PackageWithProducts pack, int membersCount) {
        pack.getDayWeights().forEach((day, weight) -> {
            double perPerson = weight / membersCount;
            addLoad(day, perPerson);
            assignPackage(day, pack);
        });
    }

    /** Створити копію стану для гілки алгоритму */
    public HikerState cloneState() {
        HikerState copy = this.toBuilder().build();
        copy.getLoadByDay().clear();
        copy.getLoadByDay().putAll(this.loadByDay);
        copy.getTargetByDay().clear();
        copy.getTargetByDay().putAll(this.targetByDay);
        copy.getAssignedPackages().clear();
        copy.getAssignedPackages().addAll(this.assignedPackages);
        copy.getAssignedByDay().clear();
        this.assignedByDay.forEach((day, packs) -> copy.getAssignedByDay().put(day, new LinkedHashSet<>(packs)));
        return copy;
    }
}
