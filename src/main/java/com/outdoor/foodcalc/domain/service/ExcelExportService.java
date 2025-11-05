package com.outdoor.foodcalc.domain.service;

import com.outdoor.foodcalc.domain.model.plan.FoodPlan;
import com.outdoor.foodcalc.domain.model.plan.pack.HikerState;
import com.outdoor.foodcalc.domain.model.plan.pack.PackageWithProducts;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcelExportService {
    public XSSFWorkbook exportFoodPlan(FoodPlan plan, List<HikerState> bestStates, List<PackageWithProducts> packages, List<LocalDate> sortedDates) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.getProperties().getCoreProperties().setTitle(plan.getName());

        // стилі
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle borderStyle = createBorderStyle(workbook);
        CellStyle decimalStyle = createDecimalStyle(workbook, borderStyle);
        CellStyle percentStyle = createPercentStyle(workbook, borderStyle);

        int membersCount = plan.getMembers().size();

        // Distribution
        createDistributionSheet(workbook, bestStates, membersCount, headerStyle, decimalStyle);

        // Details
        createDetailsSheet(workbook, bestStates, sortedDates, headerStyle, decimalStyle, percentStyle);

        // Packages
        createPackagesSheet(workbook, packages, sortedDates, membersCount, headerStyle, decimalStyle);

        return workbook;
    }

    // Стилі
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBorderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // стиль для чисел (0.00)
    private CellStyle createDecimalStyle(XSSFWorkbook workbook, CellStyle borderStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(borderStyle);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        return style;
    }

    // стиль для відсотків (0.00%)
    private CellStyle createPercentStyle(XSSFWorkbook workbook, CellStyle baseBorderStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseBorderStyle);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00%"));
        return style;
    }

    private void createHeader(Row headerRow, String[] headers, CellStyle style) {
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void createDistributionSheet(XSSFWorkbook workbook,
                                         List<HikerState> bestStates,
                                         int membersCount,
                                         CellStyle headerStyle,
                                         CellStyle decimalStyle) {

        Sheet sheet = workbook.createSheet("Distribution");
        int rowNum = 0;

        Row header = sheet.createRow(rowNum++);
        String[] headers = {"Tourist", "Assigned Packages", "Weight"};
        createHeader(header, headers, headerStyle);

        for (HikerState hiker : bestStates) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(hiker.getHiker().getName());

            String packages = hiker.getAssignedPackages().stream()
                    .map(p -> p.getFoodPackage().getName())
                    .sorted()
                    .collect(Collectors.joining(", "));
            row.createCell(1).setCellValue(packages);

            double totalWeight = hiker.getAssignedPackages().stream()
                    .mapToDouble(PackageWithProducts::getEstimatedWeight)
                    .sum();

            Cell weightCell = row.createCell(2);
            weightCell.setCellStyle(decimalStyle);
            weightCell.setCellValue(totalWeight);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createDetailsSheet(XSSFWorkbook workbook,
                                    List<HikerState> states,
                                    List<LocalDate> sortedDates,
                                    CellStyle headerStyle,
                                    CellStyle decimalStyle,
                                    CellStyle percentStyle) {

        Sheet sheet = workbook.createSheet("Details");
        int rowNum = 0;

        String[] headers = {"Hiker", "Date", "Target For Day", "Assigned Weight For Day", "Deviation, %", "Assigned Packages"};
        Row header = sheet.createRow(rowNum++);
        createHeader(header, headers, headerStyle);

        for (HikerState hiker : states) {
            double totalTarget = 0.0;
            double totalAssigned = 0.0;

            for (LocalDate day : sortedDates) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(hiker.getHiker().getName());
                row.createCell(1).setCellValue(day.toString());

                double target = hiker.getTargetByDay().getOrDefault(day, 0.0);
                double assigned = hiker.getLoadByDay().getOrDefault(day, 0.0);
                double deviation = (target != 0) ? (assigned - target) / target : 0.0;

                Cell tCell = row.createCell(2);
                tCell.setCellStyle(decimalStyle);
                tCell.setCellValue(target);

                Cell aCell = row.createCell(3);
                aCell.setCellStyle(decimalStyle);
                aCell.setCellValue(assigned);

                Cell devCell = row.createCell(4);
                devCell.setCellStyle(percentStyle);
                devCell.setCellValue(deviation);

                Set<PackageWithProducts> packs = hiker.getAssignedByDay().getOrDefault(day, Set.of());
                row.createCell(5).setCellValue(
                        packs.stream()
                                .map(p -> p.getFoodPackage().getName())
                                .sorted()
                                .collect(Collectors.joining(", "))
                );

                totalTarget += target;
                totalAssigned += assigned;
            }

            Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("total");

            Cell tTotal = totalRow.createCell(2);
            tTotal.setCellStyle(decimalStyle);
            tTotal.setCellValue(totalTarget);

            Cell aTotal = totalRow.createCell(3);
            aTotal.setCellStyle(decimalStyle);
            aTotal.setCellValue(totalAssigned);

            double totalDeviation = (totalTarget != 0) ? (totalAssigned - totalTarget) / totalTarget : 0.0;
            Cell devTotal = totalRow.createCell(4);
            devTotal.setCellStyle(percentStyle);
            devTotal.setCellValue(totalDeviation);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createPackagesSheet(XSSFWorkbook workbook,
                                     List<PackageWithProducts> packages,
                                     List<LocalDate> sortedDates,
                                     int membersCount,
                                     CellStyle headerStyle,
                                     CellStyle decimalStyle) {

        Sheet sheet = workbook.createSheet("Packages");
        int rowNum = 0;

        Row header = sheet.createRow(rowNum++);
        int col = 0;
        header.createCell(col++).setCellValue("name");
        header.createCell(col++).setCellValue("vol coef");
        header.createCell(col++).setCellValue("addPack");
        header.createCell(col++).setCellValue("full");

        List<LocalDate> reversedDates = new ArrayList<>(sortedDates);
        reversedDates.sort(Comparator.reverseOrder());
        for (LocalDate day : reversedDates) {
            header.createCell(col++).setCellValue(day.toString());
        }

        for (int i = 0; i < header.getLastCellNum(); i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }

        for (PackageWithProducts pack : packages) {
            Row row = sheet.createRow(rowNum++);
            int c = 0;

            row.createCell(c++).setCellValue(pack.getFoodPackage().getName());

            Cell volCell = row.createCell(c++);
            volCell.setCellStyle(decimalStyle);
            volCell.setCellValue(pack.getFoodPackage().getVolumeCoefficient());

            Cell addCell = row.createCell(c++);
            addCell.setCellStyle(decimalStyle);
            addCell.setCellValue(pack.getFoodPackage().getAdditionalWeight());

            double fullWeight = pack.getEstimatedWeight();
            Cell fullCell = row.createCell(c++);
            fullCell.setCellStyle(decimalStyle);
            fullCell.setCellValue(fullWeight);

            for (LocalDate day : reversedDates) {
                double dayWeight = pack.getWeightForDay(day, membersCount);
                Cell cell = row.createCell(c++);
                cell.setCellStyle(decimalStyle);
                cell.setCellValue(dayWeight);
            }
        }

        for (int i = 0; i < header.getLastCellNum(); i++) sheet.autoSizeColumn(i);
    }
}
