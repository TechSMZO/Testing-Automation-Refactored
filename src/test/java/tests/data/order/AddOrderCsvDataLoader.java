package tests.data.order;

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
import tests.model.order.AddOrderCaseData;

/**
 * Loads add-order datasets from CSV using Apache POI sheet/row/cell APIs.
 */
public final class AddOrderCsvDataLoader {
    private AddOrderCsvDataLoader() {
    }

    public static List<AddOrderCaseData> loadAddOrderCasesData(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("CSV file not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("add_order_data");
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
                    "scenario_id", "buyer_name", "buyer_phone", "address_line1", "pincode",
                    "product_name", "quantity", "unit_price", "payment_type", "total_weight",
                    "length", "width", "height", "run_enabled"
            };
            for (String requiredColumn : required) {
                if (!columnMap.containsKey(requiredColumn)) {
                    throw new IllegalStateException(
                            "Required column missing (" + requiredColumn + "): " + resourcePath);
                }
            }

            List<AddOrderCaseData> rows = new ArrayList<>();
            Map<String, Integer> scenarioCounter = new HashMap<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String scenarioId = value(formatter, row, columnMap, "scenario_id");
                String buyerName = value(formatter, row, columnMap, "buyer_name");
                String buyerPhone = value(formatter, row, columnMap, "buyer_phone");
                String buyerAlternatePhone = value(formatter, row, columnMap, "buyer_alternate_phone");
                String buyerEmail = value(formatter, row, columnMap, "buyer_email");
                String buyerGst = value(formatter, row, columnMap, "buyer_gst");
                String addressLine1 = value(formatter, row, columnMap, "address_line1");
                String addressLine2 = value(formatter, row, columnMap, "address_line2");
                String pincode = value(formatter, row, columnMap, "pincode");
                String orderDate = value(formatter, row, columnMap, "order_date");
                String orderType = value(formatter, row, columnMap, "order_type");
                String productName = value(formatter, row, columnMap, "product_name");
                String quantity = value(formatter, row, columnMap, "quantity");
                String unitPrice = value(formatter, row, columnMap, "unit_price");
                String paymentType = value(formatter, row, columnMap, "payment_type");
                String codMode = value(formatter, row, columnMap, "cod_mode");
                String codAmount = value(formatter, row, columnMap, "cod_amount");
                String shippingCharges = value(formatter, row, columnMap, "shipping_charges");
                String totalWeight = value(formatter, row, columnMap, "total_weight");
                String length = value(formatter, row, columnMap, "length");
                String width = value(formatter, row, columnMap, "width");
                String height = value(formatter, row, columnMap, "height");
                String resellerName = value(formatter, row, columnMap, "reseller_name");
                String resellerGst = value(formatter, row, columnMap, "reseller_gst");
                String ewayBill = value(formatter, row, columnMap, "eway_bill");
                String warehouseTitle = value(formatter, row, columnMap, "warehouse_title");
                String warehousePhone = value(formatter, row, columnMap, "warehouse_phone");
                String warehouseEmail = value(formatter, row, columnMap, "warehouse_email");
                String runEnabledRaw = value(formatter, row, columnMap, "run_enabled");

                if (scenarioId.isEmpty() && buyerName.isEmpty() && buyerPhone.isEmpty() && addressLine1.isEmpty()
                        && pincode.isEmpty() && productName.isEmpty()) {
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
                rows.add(new AddOrderCaseData(
                        scenarioId,
                        buyerName,
                        buyerPhone,
                        buyerAlternatePhone,
                        buyerEmail,
                        buyerGst,
                        addressLine1,
                        addressLine2,
                        pincode,
                        orderDate,
                        orderType,
                        productName,
                        quantity,
                        unitPrice,
                        paymentType,
                        codMode,
                        codAmount,
                        shippingCharges,
                        totalWeight,
                        length,
                        width,
                        height,
                        resellerName,
                        resellerGst,
                        ewayBill,
                        warehouseTitle,
                        warehousePhone,
                        warehouseEmail,
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
