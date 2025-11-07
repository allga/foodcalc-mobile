package com.outdoor.foodcalc.domain.service;


import com.outdoor.foodcalc.domain.model.plan.FoodPlan;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Standalone version of Manual Branch and Bound algorithm for FoodCalc Mobile.
 */
public class ManualBnBDistributionService {

    private List<LocalDate> sortedDates;
    private Map<LocalDate, List<PackageWithProducts>> packagesByDate;
    private List<HikerState> bestSolution;
    private int membersCount;
    private double bestDeviation = Double.MAX_VALUE;
    private static final double TOLERANCE = 0.10;


    // Головний метод — пошук найкращого розподілу
    public List<HikerState> findBestDistribution(FoodPlan plan) {

        this.membersCount = plan.getMembers().size();
        List<PackageWithProducts> packages = plan.getPackages();
        prepareData(packages);
        // Ініціалізація станів туристів
        List<HikerState> hikers = plan.getMembers().stream()
                .map(HikerState::new)
                .collect(Collectors.toList());

        // Розрахунок групових таргетів
        Map<LocalDate, Double> groupTargets = calculateGroupTargets(sortedDates);
        // Розрахунок індивідуальних таргетів
        calculateIndividualTargets(plan, hikers, groupTargets);

        // запускаємо алгоритм branchAndBound
        branchAndBound(0, hikers);

        if (bestSolution == null)
            throw new RuntimeException("No valid solution found");

        return bestSolution;
    }

    // Рекурсивний обхід дерева рішень (Branch and Bound)
    private void branchAndBound(int dayIndex, List<HikerState> hikers) {

        // базовий випадок: усі дні розподілені
        if (dayIndex >= sortedDates.size()) {

            // зберігаємо тільки найкраще рішення
            double deviation = calculateTotalDeviation(hikers);
            if (deviation < bestDeviation) {
                bestDeviation = deviation;
                bestSolution = hikers.stream()
                        .map(HikerState::cloneState)
                        .collect(Collectors.toList());
            }
            return; // рішення вже збережено в assignPackagesOfDay
        }

        LocalDate currentDay = sortedDates.get(dayIndex);
        List<PackageWithProducts> remainingPackages = getUnassignedPackages(hikers, currentDay);

        // пакунки спадають за вагою поточного дня
        remainingPackages.sort(Comparator.comparingDouble(
                p -> -p.getWeightForDay(currentDay)));

        // якщо на день немає пакунків — просто переходимо далі
        if (remainingPackages.isEmpty()) {
            branchAndBound(dayIndex + 1, hikers);
            return;
        }

        // сортування туристів
        if (dayIndex == 0)
            // сильніші спочатку
            hikers.sort(Comparator.comparingDouble(s -> -s.getHiker().getWeightCoefficient()));
        else
            // менше завантажені — раніше
            hikers.sort(Comparator.comparingDouble(s -> s.getTotalWeightUpTo(currentDay)));

        // розподіляємо всі пакунки поточного дня
        assignPackagesOfDay(currentDay, remainingPackages, hikers, dayIndex);

        // переходимо далі
        branchAndBound(dayIndex + 1, hikers);
    }

    // Призначення пакунків
    private void assignPackagesOfDay(LocalDate currentDay,
                                     List<PackageWithProducts> remaining,
                                     List<HikerState> hikers,
                                     int dayIndex) {

        if (remaining.isEmpty()) {
            // Якщо пакунків на цей день більше немає кожен турист має бути в межах [90%; 110%] таргета
            for (HikerState hiker : hikers) {
                double target = hiker.getTargetByDay().getOrDefault(currentDay, 0.0);
                double load = hiker.getLoadForDay(currentDay);
                double minAllowed = target * (1.0 - TOLERANCE);

                if (load < minAllowed) {
                    // недовантаження — день недопустимий, не продовжуємо гілку
                    return;
                }
            }

            if (dayIndex < sortedDates.size() - 1) {
                branchAndBound(dayIndex + 1, hikers);
                return;

            } else {
                // Перевіряємо остаточну допустимість рішення перед збереженням
                boolean valid = true;

                for (HikerState hiker : hikers) {
                    for (LocalDate day : sortedDates) {
                        double target = hiker.getTargetByDay().getOrDefault(day, 0.0);
                        if (target == 0.0) continue;
                        double load = hiker.getLoadForDay(day);
                        double minAllowed = target * (1 - TOLERANCE);
                        double maxAllowed = target * (1 + TOLERANCE);

                        if (load < minAllowed || load > maxAllowed) {
                            valid = false;
                            System.out.printf("Invalid final day %s: %s load=%.1f, target=%.1f%n",
                                    day, hiker.getHiker().getName(), load, target);
                        }
                    }
                }

                // Рішення не збережено — перевищено допустимі межі навантаження
                if (!valid) return;

                // якщо це останній день — фінальна оцінка й збереження bestSolution
                double deviation = calculateTotalDeviation(hikers);
                if (deviation < bestDeviation) {
                    // Зберігаємо нове найкраще рішення
                    bestDeviation = deviation;
                    bestSolution = hikers.stream()
                            .map(HikerState::cloneState)
                            .collect(Collectors.toList());
                }
            }

            return;
        }

        // переходимо до наступного дня
        // беремо поточний пакунок
        PackageWithProducts currentPackage = remaining.get(0);
        // решта пакунків поточного дня
        List<PackageWithProducts> nextPackages = remaining.subList(1, remaining.size());

        // пробуємо призначити цей пакунок кожному туристу
        for (HikerState hiker : hikers) {
            // Створюємо копію всього списку станів (щоб гілка була незалежною)
            List<HikerState> nextStates = hikers.stream()
                    .map(HikerState::cloneState)
                    .collect(Collectors.toList());

            // Знаходимо відповідного туриста у копії
            HikerState currentHiker = nextStates.stream()
                    .filter(s -> s.getHiker().equals(hiker.getHiker()))
                    .findFirst()
                    .orElseThrow();

            // Додаємо пакунок у копію (а не в оригінал)
            currentHiker.addPackage(currentPackage, membersCount);

            // Перевіряємо допустимість
            if (isFeasible(currentHiker, currentPackage, currentDay))
                // рекурсія з новою копією станів
                assignPackagesOfDay(currentDay, nextPackages, nextStates, dayIndex);
        }
    }

    // Перевірка припустимості
    private boolean isFeasible(HikerState hiker, PackageWithProducts pack, LocalDate currentDay) {

        // Перевіряємо щоб не перевищити таргет у дні використання пакунка від currentDay і далі
        for (LocalDate day : pack.getDayWeights().keySet()) {
            if (day.isBefore(currentDay)) continue; // пропускаємо попередні дні

            // Отримуємо вже розрахований таргет
            Double target = hiker.getTargetByDay().get(day);
            if (target == null) return false;

            // Поточне навантаження (включно з усіма призначеними пакунками)
            double load = hiker.getLoadForDay(day);

            // Визначаємо верхню межу допустимого відхилення
            double maxAllowed = target * (1.0 + TOLERANCE);

            // Перевіряємо чи навантаження в межах
            // Якщо розподіл ще триває — перевіряємо тільки верхню межу
            boolean feasible = load <= maxAllowed;
            if (!feasible) return false;
        }

        return true;
    }

    // Нерозподілені пакунки
    private List<PackageWithProducts> getUnassignedPackages(List<HikerState> hikers, LocalDate day) {
        Set<PackageWithProducts> assigned = hikers.stream()
                .flatMap(s -> s.getAssignedPackages().stream())
                .collect(Collectors.toSet());
        return packagesByDate.getOrDefault(day, Collections.emptyList()).stream()
                .filter(p -> !assigned.contains(p))
                .collect(Collectors.toList());
    }

    // Групування пакунків за датами
    private void prepareData(List<PackageWithProducts> packages) {
        packagesByDate = new HashMap<>();
        for (PackageWithProducts pack : packages) {
            for (LocalDate d : pack.getDayWeights().keySet()) {
                packagesByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(pack);
            }
        }
        sortedDates = new ArrayList<>(packagesByDate.keySet());
        sortedDates.sort(Comparator.reverseOrder());
    }
    
    // Розрахунок групових таргетів для кожного дня
    private Map<LocalDate, Double> calculateGroupTargets(List<LocalDate> days) {
        Map<LocalDate, Double> groupTargets = new HashMap<>();
        for (LocalDate day : days) {
            double total = packagesByDate.getOrDefault(day, List.of()).stream()
                    .mapToDouble(p -> p.getWeightForDay(day))
                    .sum();
            groupTargets.put(day, total);
        }
        return groupTargets;
    }

    // Розрахунок індивідуальних таргетів для кожного туриста
    private void calculateIndividualTargets(FoodPlan plan, List<HikerState> hikers,
                                            Map<LocalDate, Double> groupTargets) {
        double totalCoeff = plan.getMembers().stream()
                .mapToDouble(h -> h.getWeightCoefficient())
                .sum();

        for (HikerState hiker : hikers) {
            for (LocalDate day : sortedDates) {
                double groupTarget = groupTargets.getOrDefault(day, 0.0);
                double target = groupTarget * (hiker.getHiker().getWeightCoefficient() / totalCoeff);
                hiker.setTargetByDay(day, target);
            }
        }
    }

    // Обчислення середнього відхилення від таргету по всіх туристах і днях
    private double calculateTotalDeviation(List<HikerState> hikers) {
        double total = 0;
        int count = 0;
        for (HikerState hiker : hikers) {
            for (LocalDate day : sortedDates) {
                Double target = hiker.getTargetByDay().get(day);
                if (target == null || target == 0) continue;
                double load = hiker.getLoadForDay(day);
                double deviation = Math.abs(load - target) / target;
                total += deviation;
                count++;
            }
        }
        return (count == 0) ? Double.MAX_VALUE : (total / count) * 100.0;
    }
}
