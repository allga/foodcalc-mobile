package com.outdoor.foodcalc;

import com.outdoor.foodcalc.domain.model.plan.Hiker;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;
import com.outdoor.foodcalc.domain.service.ManualBnBDistributionService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // 1) тестові туристи
        List<Hiker> hikers = List.of(
                new Hiker(1, "Оля", 1.0),
                new Hiker(2, "Ігор", 1.2)
        );

        // 2) тестові пакунки: день->вага (у грамах)
        Map<Integer, Double> pack1 = new LinkedHashMap<>();
        pack1.put(1, 110.0); pack1.put(2, 100.0); pack1.put(3, 90.0);

        Map<Integer, Double> pack2 = new LinkedHashMap<>();
        pack2.put(1, 150.0); pack2.put(2, 120.0); pack2.put(3, 90.0);

        List<PackageWithProducts> packages = List.of(
                PackageWithProducts.builder().name("Тушонка").dayWeights(pack1).build(),
                PackageWithProducts.builder().name("Каша").dayWeights(pack2).build()
        );

        // 3) дні походу
        List<LocalDate> days = List.of(
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 7, 11),
                LocalDate.of(2025, 7, 12)
        );

        // 4) запуск алгоритму
        ManualBnBDistributionService service = new ManualBnBDistributionService();
        List<HikerState> result = service.distribute(hikers, packages, days);

        // 5) вивід результату
        System.out.println("\n📊 Результат розподілу:");
        for (HikerState state : result) {
            System.out.println("— " + state.getHiker().getName() + " → " + state.getLoadByDay());
        }
        System.out.println("✅ Готово.");
    }
}