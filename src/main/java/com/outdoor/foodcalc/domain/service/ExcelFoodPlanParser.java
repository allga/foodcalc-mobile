package com.outdoor.foodcalc.domain.service;

import com.outdoor.foodcalc.domain.model.plan.FoodPlan;
import com.outdoor.foodcalc.domain.model.plan.Hiker;
import com.outdoor.foodcalc.domain.model.plan.PlanDay;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер Excel-файлу, експортованого з основного FoodCalc.
 * Відновлює FoodPlan, сумісний із ManualBnBDistributionService.
 */
public class ExcelFoodPlanParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static FoodPlan parseFoodPlanFromExcel(InputStream excelStream) throws Exception {
        try (Workbook wb = new XSSFWorkbook(excelStream)) {

            FoodPlan plan = FoodPlan.builder().build();

            // План (Plan)
            Sheet planSheet = wb.getSheet("Plan");
            if (planSheet != null) {
                Row row = planSheet.getRow(1);
                if (row != null) {
                    plan.setId((long) row.getCell(0).getNumericCellValue());
                    plan.setName(row.getCell(1).getStringCellValue());
                    plan.setDescription(row.getCell(2)  != null ? row.getCell(2).getStringCellValue() : null);
                    plan.setSchemaVersion( row.getCell(3) != null ? row.getCell(3).getStringCellValue() : null );
                }
            }

            // Учасники (Hikers)
            Sheet hikersSheet = wb.getSheet("Hikers");
            List<Hiker> hikers = new ArrayList<>();
            if (hikersSheet != null) {
                for (int r = 1; r <= hikersSheet.getLastRowNum(); r++) {
                    Row row = hikersSheet.getRow(r);
                    if (row == null) continue;
                    Hiker h = Hiker.builder()
                            .id((long) row.getCell(0).getNumericCellValue())
                            .name(row.getCell(1).getStringCellValue())
                            .description(row.getCell(2) != null ? row.getCell(2).getStringCellValue() : "")
                            .weightCoefficient((float) row.getCell(3).getNumericCellValue())
                            .build();
                    hikers.add(h);
                }
            }
            plan.setMembers(hikers);

            // Дні (Days)
            Sheet daysSheet = wb.getSheet("Days");
            List<PlanDay> days = new ArrayList<>();

            if (daysSheet != null) {
                for (int r = 1; r <= daysSheet.getLastRowNum(); r++) {
                    Row row = daysSheet.getRow(r);
                    if (row == null) continue;

                    long id = (long) row.getCell(0).getNumericCellValue();
                    LocalDate date = LocalDate.parse(row.getCell(1).getStringCellValue(), DATE_FMT);
                    String desc = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : "";

                    days.add(PlanDay.builder()
                            .dayId(id)
                            .date(date)
                            .description(desc)
                            .build());                }
            }
            plan.setDays(days);

            // Пакунки (Packages)
            Sheet packagesSheet = wb.getSheet("Packages");
            List<PackageWithProducts> packages = new ArrayList<>();

            if (packagesSheet != null) {
                Row header = packagesSheet.getRow(0);
                // перші 4 колонки: name, vol coef, addPack, full
                List<LocalDate> dates = new ArrayList<>();
                for (int i = 4; i < header.getLastCellNum(); i++) {
                    Cell c = header.getCell(i);
                    if (c != null && c.getCellType() == CellType.STRING) {
                        dates.add(LocalDate.parse(c.getStringCellValue(), DATE_FMT));
                    }
                }

                for (int r = 1; r <= packagesSheet.getLastRowNum(); r++) {
                    Row row = packagesSheet.getRow(r);
                    if (row == null) continue;

                    String name = row.getCell(0).getStringCellValue().trim();
                    double vol = getNumericCellValueSafe(row.getCell(1));
                    double addW = getNumericCellValueSafe(row.getCell(2));
                    double fullW = getNumericCellValueSafe(row.getCell(3));

                    Map<LocalDate, Double> dayWeights = new LinkedHashMap<>();
                    int daysStartCol  = 4;
                    for (LocalDate d : dates) {
                        Cell weightCell = row.getCell(daysStartCol++);
                        if (weightCell != null && weightCell.getCellType() == CellType.NUMERIC) {
                            dayWeights.put(d, weightCell.getNumericCellValue());
                        } else {
                            dayWeights.put(d, 0.0);
                        }
                    }

                    PackageWithProducts pack = PackageWithProducts.builder()
                            .name(name)
                            .volumeCoefficient(vol)
                            .additionalWeight(addW)
                            .fullWeight(fullW)
                            .dayWeights(dayWeights)
                            .build();
                    packages.add(pack);
                }
            }

            plan.setPackages(packages);

            return plan;
        }
    }

    private static double getNumericCellValueSafe(Cell cell) {
        if (cell == null) return 0.0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
}
