package com.outdoor.foodcalc.runner;

import com.outdoor.foodcalc.domain.model.plan.FoodPlan;
import com.outdoor.foodcalc.domain.model.plan.PlanDay;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.service.ExcelExportService;
import com.outdoor.foodcalc.domain.service.ExcelFoodPlanParser;
import com.outdoor.foodcalc.domain.service.ManualBnBDistributionService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class FoodPlanRunner {

    public static void main(String[] args) {
        try {
            // Прочитати Excel з планом
            String inputPath = "D:/foodcalcData/foodPlanTS.xlsx";
            InputStream in = new FileInputStream(inputPath);

            FoodPlan plan = ExcelFoodPlanParser.parseFoodPlanFromExcel(in);
            System.out.println("Plan loaded: " + plan.getName());

            // Запустити алгоритм розподілу
            ManualBnBDistributionService service = new ManualBnBDistributionService();
            List<HikerState> result = service.findBestDistribution(plan);

            // Експорт результатів розподілу
            ExcelExportService exporter = new ExcelExportService();
            try (XSSFWorkbook workbook = exporter.exportFoodPlan(
                    plan, result, plan.getPackages(), plan.getDays().stream()
                            .map(PlanDay::getDate).collect(Collectors.toList()));
                 FileOutputStream out = new FileOutputStream("D:/foodcalcData/foodplan_result.xlsx")) {

                workbook.write(out);
                System.out.println("Result exported to: D:/foodcalcData/foodplan_result.xlsx");
            }

            for (HikerState state : result) {
                System.out.println(state);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}
