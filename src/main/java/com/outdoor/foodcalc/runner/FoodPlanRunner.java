package com.outdoor.foodcalc.runner;

import com.outdoor.foodcalc.domain.model.plan.FoodPlan;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.service.ExcelExportService;
import com.outdoor.foodcalc.domain.service.ExcelFoodPlanParser;
import com.outdoor.foodcalc.domain.service.ManualBnBDistributionService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

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

            System.out.println("Result");
            for (HikerState state : result) {
                System.out.println(state);
            }





// Експортувати результат у новий Excel
//            ExcelExportService exporter = new ExcelExportService();
//            XSSFWorkbook workbook = exporter.exportFoodPlan(result);
//
//            String outputPath = "D:/foodplan_result.xlsx";
//            try (FileOutputStream out = new FileOutputStream(outputPath)) {
//                workbook.write(out);
//            }
//
//            System.out.println("Result exported to: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }
}
