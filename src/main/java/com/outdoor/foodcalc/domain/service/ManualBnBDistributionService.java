package com.outdoor.foodcalc.domain.service;

import com.outdoor.foodcalc.domain.model.plan.Hiker;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Simplified Branch & Bound distribution algorithm for FoodCalc Mobile.
 * Works without Spring and repositories.
 */

/**
* Як це працює
* List<HikerState> — створюється початковий стан для кожного туриста.
* for (PackageWithProducts pack : packages) — проходить усі пакунки.
* assignPackageToHiker() — розподіляє вагу по днях згідно з dayWeights.
* return states — результатом є повний розподіл навантаження по туристах.
*/

/**
 * Що ще можна буде додати пізніше
* сортування туристів за коефіцієнтом навантаження (loadRatio);
* відсікання гілок (BnB core);
* перевірку монотонності (щоб вага спадала по днях);
* таймер вимірювання часу виконання;
* інтеграцію з Excel-експортом.
*/
public class ManualBnBDistributionService {

    /**
     * Performs load distribution among hikers for the given day packages.
     *
     * @param hikers       list of hikers with load ratios
     * @param packages     list of packages to assign
     * @param sortedDates  ordered list of trip days
     * @return map of hiker states with assigned daily loads
     */
    public List<HikerState> distribute(List<Hiker> hikers,
                                       List<PackageWithProducts> packages,
                                       List<LocalDate> sortedDates) {
        // ініціалізація станів туристів
        List<HikerState> states = new ArrayList<>();
        for (Hiker h : hikers) {
            states.add(HikerState.builder().hiker(h).loadByDay(new LinkedHashMap<>()).build());
        }

        System.out.println(" Starting BnB distribution for " + sortedDates.size() + " days...");

        // СПРОЩЕНА ВЕРСІЯ: базовий рівномірний розподіл
        // (далі можна вставити реальний BnB-алгоритм)
        int i = 0;
        for (PackageWithProducts pack : packages) {
            HikerState current = states.get(i % states.size());
            assignPackageToHiker(current, pack, sortedDates);
            i++;
        }

        System.out.println("Distribution complete.");
        return states;
    }

    /**
     * Assigns package weights to hiker state across trip days.
     */
    private void assignPackageToHiker(HikerState state,
                                      PackageWithProducts pack,
                                      List<LocalDate> sortedDates) {

        for (int dayIndex = 0; dayIndex < sortedDates.size(); dayIndex++) {
            LocalDate day = sortedDates.get(dayIndex);
            double weight = pack.getDayWeights().getOrDefault(dayIndex + 1, 0.0);
            if (weight > 0) {
                state.addLoad(day, weight);
            }
        }
    }
}
