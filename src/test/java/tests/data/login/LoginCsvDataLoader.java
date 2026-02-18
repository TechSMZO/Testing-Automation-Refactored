package tests.data.login;

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
import tests.model.login.LoginCaseData;

/**
 * Loads login datasets from CSV using Apache POI sheet/row/cell APIs.
 */
public final class LoginCsvDataLoader {
    private LoginCsvDataLoader() {
    }

    public static List<LoginCaseData> loadLoginCasesData(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("CSV file not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("login_data");
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
            int scenarioCol = -1;
            int usernameCol = -1;
            int passwordCol = -1;

            for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                String header = formatter.formatCellValue(headerRow.getCell(col)).trim().toLowerCase();
                if ("scenario_id".equals(header)) {
                    scenarioCol = col;
                } else if ("username".equals(header)) {
                    usernameCol = col;
                } else if ("password".equals(header)) {
                    passwordCol = col;
                }
            }

            if (scenarioCol < 0 || usernameCol < 0 || passwordCol < 0) {
                throw new IllegalStateException(
                        "Required columns missing (scenario_id, username, password): " + resourcePath);
            }

            List<LoginCaseData> rows = new ArrayList<>();
            Map<String, Integer> scenarioCounter = new HashMap<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                
                String scenarioId = formatter.formatCellValue(row.getCell(scenarioCol)).trim();
                String username = formatter.formatCellValue(row.getCell(usernameCol)).trim();
                String password = formatter.formatCellValue(row.getCell(passwordCol)).trim();

                if (scenarioId.isEmpty() && username.isEmpty() && password.isEmpty()) {
                    continue;
                }
                if (scenarioId.isEmpty()) {
                    throw new IllegalStateException("scenario_id cannot be blank: " + resourcePath + " row " + (r + 1));
                }

                int datasetIndex = scenarioCounter.getOrDefault(scenarioId, 0) + 1;
                scenarioCounter.put(scenarioId, datasetIndex);
                rows.add(new LoginCaseData(scenarioId, username, password, datasetIndex));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV: " + resourcePath, exception);
        }
    }
}
