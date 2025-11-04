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

    public List<HikerState> findBestDistribution(FoodPlan plan) {
        this.membersCount = plan.getMembers().size();
        List<PackageWithProducts> packages = plan.getPackages();
        prepareData(packages);

        // Ініціалізація станів
        Map<LocalDate, Double> groupTargets = calculateGroupTargets(sortedDates);
        List<HikerState> states = plan.getMembers().stream()
                .map(HikerState::new)
                .collect(Collectors.toList());

        // Розрахунок індивідуальних таргетів
        calculateIndividualTargets(plan, states, groupTargets);

        branchAndBound(0, states);

        if (bestSolution == null)
            throw new RuntimeException("No valid solution found");

        return bestSolution;
    }

    private void branchAndBound(int dayIndex, List<HikerState> states) {
        if (dayIndex >= sortedDates.size()) {
            double deviation = calculateTotalDeviation(states);
            if (deviation < bestDeviation) {
                bestDeviation = deviation;
                bestSolution = states.stream()
                        .map(HikerState::cloneState)
                        .collect(Collectors.toList());
            }
            return;
        }

        LocalDate currentDay = sortedDates.get(dayIndex);
        List<PackageWithProducts> remaining = getUnassignedPackages(states, currentDay);

        // пакунки (спадають за вагою поточного дня)
        remaining.sort(Comparator.comparingDouble(p -> -p.getWeightForDay(currentDay, membersCount)));

        if (remaining.isEmpty()) {
            branchAndBound(dayIndex + 1, states);
            return;
        }

        // туристи
        if (dayIndex == 0)
            // сильніші спочатку
            states.sort(Comparator.comparingDouble(s -> -s.getHiker().getWeightCoefficient()));
        else
            // менше завантажені — раніше
            states.sort(Comparator.comparingDouble(s -> s.getTotalWeightUpTo(currentDay)));

        assignPackagesOfDay(currentDay, remaining, states, dayIndex);
        branchAndBound(dayIndex + 1, states);
    }

    // Призначення пакунків
    private void assignPackagesOfDay(LocalDate currentDay,
                                     List<PackageWithProducts> remaining,
                                     List<HikerState> states,
                                     int dayIndex) {

        // Якщо пакунків на цей день більше немає — переходимо до наступного дня
        if (remaining.isEmpty()) {
            if (dayIndex < sortedDates.size() - 1) {
                branchAndBound(dayIndex + 1, states);
            } else {
                // фінальна оцінка й збереження bestSolution
                double deviation = calculateTotalDeviation(states);
                if (deviation < bestDeviation) {
                    bestDeviation = deviation;
                    bestSolution = states.stream()
                            .map(HikerState::cloneState)
                            .collect(Collectors.toList());
                }
            }
            return;
        }

        PackageWithProducts currentPackage = remaining.get(0);
        List<PackageWithProducts> next = remaining.subList(1, remaining.size());

        for (HikerState hiker : states) {
            List<HikerState> nextStates = states.stream()
                    .map(HikerState::cloneState)
                    .collect(Collectors.toList());

            HikerState currentHiker = nextStates.stream()
                    .filter(s -> s.getHiker().equals(hiker.getHiker()))
                    .findFirst()
                    .orElseThrow();

            currentHiker.addPackage(currentPackage, membersCount);

            if (isFeasible(currentHiker, currentPackage, currentDay))
                assignPackagesOfDay(currentDay, next, nextStates, dayIndex);
        }
    }

    // Перевірка припустимості
    private boolean isFeasible(HikerState hiker, PackageWithProducts pack, LocalDate currentDay) {
        double tol = 0.1;

        for (LocalDate day : pack.getDayWeights().keySet()) {
            if (day.isBefore(currentDay)) continue;

            double target = hiker.getTargetByDay().getOrDefault(day, 0.0);
            double load = hiker.getLoadForDay(day);
            if (load > target * (1 + tol)) return false;
        }
        return true;
    }

    // Нерозподілені пакунки
    private List<PackageWithProducts> getUnassignedPackages(List<HikerState> states, LocalDate day) {
        Set<PackageWithProducts> assigned = states.stream()
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
    
    // Таргети 
    private Map<LocalDate, Double> calculateGroupTargets(List<LocalDate> days) {
        Map<LocalDate, Double> groupTargets = new HashMap<>();
        for (LocalDate day : days) {
            double total = packagesByDate.getOrDefault(day, List.of()).stream()
                    .mapToDouble(p -> p.getWeightForDay(day, membersCount))
                    .sum();
            groupTargets.put(day, total);
        }
        return groupTargets;
    }

    private void calculateIndividualTargets(FoodPlan plan, List<HikerState> states,
                                            Map<LocalDate, Double> groupTargets) {
        double totalCoeff = plan.getMembers().stream()
                .mapToDouble(h -> h.getWeightCoefficient())
                .sum();

        for (HikerState state : states) {
            for (LocalDate day : sortedDates) {
                double groupTarget = groupTargets.getOrDefault(day, 0.0);
                double target = groupTarget * (state.getHiker().getWeightCoefficient() / totalCoeff);
                state.setTargetByDay(day, target);
            }
        }
    }

    // Оцінка рішення
    private double calculateTotalDeviation(List<HikerState> states) {
        double total = 0;
        int count = 0;
        for (HikerState state : states) {
            for (LocalDate day : sortedDates) {
                Double target = state.getTargetByDay().get(day);
                if (target == null || target == 0) continue;
                double load = state.getLoadForDay(day);
                total += Math.abs(load - target) / target;
                count++;
            }
        }
        return (count == 0) ? Double.MAX_VALUE : (total / count) * 100.0;
    }
}
