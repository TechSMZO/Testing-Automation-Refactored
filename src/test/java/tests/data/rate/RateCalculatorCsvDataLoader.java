package tests.data.rate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import tests.model.rate.RateCalculatorCaseData;

/**
 * Loads rate-calculator datasets from CSV using Apache POI sheet/row/cell APIs.
 */
public final class RateCalculatorCsvDataLoader {
    private RateCalculatorCsvDataLoader() {
    }

    public static List<RateCalculatorCaseData> loadRateCalculatorCasesData(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("CSV file not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("rate_calculator_data");
            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",", -1);
                Row row = sheet.createRow(rowNumber++);
                for (int col = 0; col < columns.length; col++) {
                    row.createCell(col).setCellValue(columns[col].trim());
                }
            }

            if (rowNumber == 0) {
                throw new IllegalStateException("CSV header is missing: " + resourcePath);
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnMap = new HashMap<>();
            for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                String header = formatter.formatCellValue(headerRow.getCell(col)).trim().toLowerCase();
                columnMap.put(header, col);
            }

            String[] required = {
                    "scenario_id",
                    "calculator_mode",
                    "package_type",
                    "origin_pincode",
                    "destination_pincode",
                    "weight",
                    "invoice_value",
                    "length",
                    "width",
                    "height",
                    "run_enabled"
            };
            for (String requiredColumn : required) {
                if (!columnMap.containsKey(requiredColumn)) {
                    throw new IllegalStateException(
                            "Required column missing (" + requiredColumn + "): " + resourcePath);
                }
            }

            List<RateCalculatorCaseData> rows = new ArrayList<>();
            Map<String, Integer> scenarioCounter = new HashMap<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String scenarioId = value(formatter, row, columnMap, "scenario_id");
                String calculatorMode = value(formatter, row, columnMap, "calculator_mode");
                String packageType = value(formatter, row, columnMap, "package_type");
                String originPincode = value(formatter, row, columnMap, "origin_pincode");
                String destinationPincode = value(formatter, row, columnMap, "destination_pincode");
                String weight = value(formatter, row, columnMap, "weight");
                String invoiceValue = value(formatter, row, columnMap, "invoice_value");
                String length = value(formatter, row, columnMap, "length");
                String width = value(formatter, row, columnMap, "width");
                String height = value(formatter, row, columnMap, "height");
                String runEnabledRaw = value(formatter, row, columnMap, "run_enabled");

                if (scenarioId.isEmpty() && originPincode.isEmpty() && destinationPincode.isEmpty() && weight.isEmpty()
                        && invoiceValue.isEmpty() && length.isEmpty() && width.isEmpty() && height.isEmpty()) {
                    continue;
                }
                if (scenarioId.isEmpty()) {
                    throw new IllegalStateException("scenario_id cannot be blank: " + resourcePath + " row " + (r + 1));
                }

                boolean runEnabled = true;
                if (!runEnabledRaw.isEmpty()) {
                    String lowered = runEnabledRaw.toLowerCase();
                    runEnabled = "true".equals(lowered) || "1".equals(lowered)
                            || "yes".equals(lowered) || "y".equals(lowered);
                }

                int datasetIndex = scenarioCounter.getOrDefault(scenarioId, 0) + 1;
                scenarioCounter.put(scenarioId, datasetIndex);
                rows.add(new RateCalculatorCaseData(
                        scenarioId,
                        calculatorMode,
                        packageType,
                        originPincode,
                        destinationPincode,
                        weight,
                        invoiceValue,
                        length,
                        width,
                        height,
                        runEnabled,
                        datasetIndex));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV: " + resourcePath, exception);
        }
    }

    private static String value(DataFormatter formatter, Row row, Map<String, Integer> columnMap, String columnName) {
        Integer index = columnMap.get(columnName);
        if (index == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }
}
