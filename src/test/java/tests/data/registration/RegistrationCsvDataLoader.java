package tests.data.registration;

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
import tests.model.registration.RegistrationCaseData;

/**
 * Loads registration datasets from CSV using Apache POI sheet/row/cell APIs.
 */
public final class RegistrationCsvDataLoader {
    private RegistrationCsvDataLoader() {
    }

    public static List<RegistrationCaseData> loadRegistrationCasesData(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("CSV file not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("registration_data");
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
            int userTypeCol = -1;
            int nameCol = -1;
            int emailCol = -1;
            int phoneCol = -1;
            int passwordCol = -1;
            int confirmPasswordCol = -1;
            int agreeTermsCol = -1;

            for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                String header = formatter.formatCellValue(headerRow.getCell(col)).trim().toLowerCase();
                if ("scenario_id".equals(header)) {
                    scenarioCol = col;
                } else if ("user_type".equals(header)) {
                    userTypeCol = col;
                } else if ("name".equals(header)) {
                    nameCol = col;
                } else if ("email".equals(header)) {
                    emailCol = col;
                } else if ("phone".equals(header)) {
                    phoneCol = col;
                } else if ("password".equals(header)) {
                    passwordCol = col;
                } else if ("confirm_password".equals(header)) {
                    confirmPasswordCol = col;
                } else if ("agree_terms".equals(header)) {
                    agreeTermsCol = col;
                }
            }

            if (scenarioCol < 0 || userTypeCol < 0 || nameCol < 0 || emailCol < 0 || phoneCol < 0
                    || passwordCol < 0 || confirmPasswordCol < 0 || agreeTermsCol < 0) {
                throw new IllegalStateException(
                        "Required columns missing (scenario_id,user_type,name,email,phone,password,confirm_password,agree_terms): "
                                + resourcePath);
            }

            List<RegistrationCaseData> rows = new ArrayList<>();
            Map<String, Integer> scenarioCounter = new HashMap<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String scenarioId = formatter.formatCellValue(row.getCell(scenarioCol)).trim();
                String userType = formatter.formatCellValue(row.getCell(userTypeCol)).trim();
                String name = formatter.formatCellValue(row.getCell(nameCol)).trim();
                String email = formatter.formatCellValue(row.getCell(emailCol)).trim();
                String phone = formatter.formatCellValue(row.getCell(phoneCol)).trim();
                String password = formatter.formatCellValue(row.getCell(passwordCol)).trim();
                String confirmPassword = formatter.formatCellValue(row.getCell(confirmPasswordCol)).trim();
                String agreeRaw = formatter.formatCellValue(row.getCell(agreeTermsCol)).trim().toLowerCase();

                if (scenarioId.isEmpty() && userType.isEmpty() && name.isEmpty() && email.isEmpty() && phone.isEmpty()
                        && password.isEmpty() && confirmPassword.isEmpty() && agreeRaw.isEmpty()) {
                    continue;
                }
                if (scenarioId.isEmpty()) {
                    throw new IllegalStateException("scenario_id cannot be blank: " + resourcePath + " row " + (r + 1));
                }

                boolean agreeTerms = "true".equals(agreeRaw) || "1".equals(agreeRaw)
                        || "yes".equals(agreeRaw) || "y".equals(agreeRaw);

                int datasetIndex = scenarioCounter.getOrDefault(scenarioId, 0) + 1;
                scenarioCounter.put(scenarioId, datasetIndex);
                rows.add(new RegistrationCaseData(
                        scenarioId,
                        userType,
                        name,
                        email,
                        phone,
                        password,
                        confirmPassword,
                        agreeTerms,
                        datasetIndex));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV: " + resourcePath, exception);
        }
    }
}
