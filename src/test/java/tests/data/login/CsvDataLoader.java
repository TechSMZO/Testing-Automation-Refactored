package tests.data.login;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tests.model.login.LoginCaseData;

/**
 * Loads login datasets from CSV in classpath.
 */
public final class CsvDataLoader {
    private CsvDataLoader() {
    }

    public static List<LoginCaseData> loadLoginCases(String resourcePath) {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("CSV data file not found in classpath: " + resourcePath);
        }

        List<LoginCaseData> dataRows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                throw new IllegalStateException("CSV header is missing: " + resourcePath);
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(headers);
            ensureMandatoryColumns(headerIndex, resourcePath);

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (isBlankRow(row)) {
                    continue;
                }
                dataRows.add(toLoginCaseData(row, headerIndex));
            }
        } catch (IOException | CsvValidationException exception) {
            throw new IllegalStateException("Failed to parse CSV: " + resourcePath, exception);
        }

        return dataRows;
    }

    private static Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> indexByName = new HashMap<>();
        for (int index = 0; index < headers.length; index++) {
            indexByName.put(normalize(headers[index]), index);
        }
        return indexByName;
    }

    private static void ensureMandatoryColumns(Map<String, Integer> headerIndex, String resourcePath) {
        String[] required = {
                "tc_id",
                "scenario_key",
                "description",
                "username",
                "password",
                "expected_result",
                "expected_error_text",
                "run_smoke",
                "run_regression",
                "enabled",
                "priority"
        };
        for (String key : required) {
            if (!headerIndex.containsKey(key)) {
                throw new IllegalStateException("Missing required column '" + key + "' in " + resourcePath);
            }
        }
    }

    private static LoginCaseData toLoginCaseData(String[] row, Map<String, Integer> headerIndex) {
        return new LoginCaseData(
                value(row, headerIndex, "tc_id"),
                value(row, headerIndex, "scenario_key"),
                value(row, headerIndex, "description"),
                value(row, headerIndex, "username"),
                value(row, headerIndex, "password"),
                value(row, headerIndex, "expected_result"),
                value(row, headerIndex, "expected_error_text"),
                parseBoolean(value(row, headerIndex, "run_smoke")),
                parseBoolean(value(row, headerIndex, "run_regression")),
                parseBoolean(value(row, headerIndex, "enabled")),
                parseInt(value(row, headerIndex, "priority"), 9999)
        );
    }

    private static String value(String[] row, Map<String, Integer> headerIndex, String key) {
        Integer index = headerIndex.get(key);
        if (index == null || index < 0 || index >= row.length) {
            return "";
        }
        return row[index] == null ? "" : row[index].trim();
    }

    private static boolean parseBoolean(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase();
        return "true".equals(value) || "1".equals(value) || "yes".equals(value) || "y".equals(value);
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }

    private static boolean isBlankRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
