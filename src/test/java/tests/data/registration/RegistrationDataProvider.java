package tests.data.registration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.model.registration.RegistrationCaseData;

/**
 * Provides registration datasets for TestNG.
 * Use `registrationData` for quick hardcoded runs and `registrationDataCsv` for CSV-driven runs.
 */
public final class RegistrationDataProvider {
    private static final String DATA_FILE = "testdata/registration/registration-regression.csv";

    private RegistrationDataProvider() {
    }

    /**
     * Hardcoded rows for quick local sanity execution.
     * Rows are filtered by @Test(description = "scenario_id").
     */
    @DataProvider(name = "registrationData")
    public static Object[][] registrationData(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        String rawScenarioKey = testAnnotation == null ? "" : testAnnotation.description();
        if (rawScenarioKey == null || rawScenarioKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing @Test(description=\"scenario_id\") on method: " + method.getName());
        }
        String scenarioKey = rawScenarioKey.trim();

        List<RegistrationCaseData> allRows = new ArrayList<>();
        allRows.add(new RegistrationCaseData("valid_registration", "Seller", "QA User", "qa@example.com", "9876543210",
                "Pass@1234", "Pass@1234", true, 1));
        allRows.add(new RegistrationCaseData("invalid_email", "Seller", "Bad Email User", "invalid-email", "9876543211",
                "Pass@1234", "Pass@1234", true, 1));
        allRows.add(new RegistrationCaseData("password_mismatch", "Seller", "Mismatch User", "qa2@example.com",
                "9876543212", "Pass@1234", "Different@123", true, 1));

        List<RegistrationCaseData> selected = new ArrayList<>();
        for (RegistrationCaseData row : allRows) {
            if (!scenarioKey.equals(row.getScenarioId())) {
                continue;
            }
            selected.add(row);
        }
        selected = keepSingleDatasetForSmoke(method, selected);

        if (selected.isEmpty()) {
            throw new SkipException("No rows found for scenario '" + scenarioKey + "'");
        }

        Object[][] rows = new Object[selected.size()][1];
        for (int index = 0; index < selected.size(); index++) {
            rows[index][0] = selected.get(index);
        }
        return rows;
    }

    /**
     * CSV rows filtered by scenario_id from @Test(description = "...").
     */
    @DataProvider(name = "registrationDataCsv")
    public static Object[][] registrationDataCsv(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        String rawScenarioKey = testAnnotation == null ? "" : testAnnotation.description();
        if (rawScenarioKey == null || rawScenarioKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing @Test(description=\"scenario_id\") on method: " + method.getName());
        }
        String scenarioKey = rawScenarioKey.trim();

        List<RegistrationCaseData> selected = new ArrayList<>();
        List<RegistrationCaseData> allRows = RegistrationCsvDataLoader.loadRegistrationCasesData(DATA_FILE);
        for (RegistrationCaseData row : allRows) {
            if (!scenarioKey.equals(row.getScenarioId())) {
                continue;
            }
            selected.add(row);
        }
        selected = keepSingleDatasetForSmoke(method, selected);

        if (selected.isEmpty()) {
            throw new SkipException("No rows found for scenario '" + scenarioKey + "'");
        }

        Object[][] rows = new Object[selected.size()][1];
        for (int index = 0; index < selected.size(); index++) {
            rows[index][0] = selected.get(index);
        }
        return rows;
    }

    private static List<RegistrationCaseData> keepSingleDatasetForSmoke(
            Method method, List<RegistrationCaseData> selected) {
        if (!isSmokeMethod(method) || selected.size() <= 1) {
            return selected;
        }
        List<RegistrationCaseData> singleRow = new ArrayList<>();
        singleRow.add(selected.get(0));
        return singleRow;
    }

    private static boolean isSmokeMethod(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation == null) {
            return false;
        }
        for (String group : testAnnotation.groups()) {
            if ("smoke".equalsIgnoreCase(group.trim())) {
                return true;
            }
        }
        return false;
    }
}
