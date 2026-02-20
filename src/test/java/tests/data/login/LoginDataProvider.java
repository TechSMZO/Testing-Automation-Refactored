package tests.data.login;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.SkipException;
import tests.model.login.LoginCaseData;

/**
 * Provides login datasets for TestNG.
 * Use `loginData` for quick hardcoded runs and `loginDataCsv` for CSV-driven runs.
 */
public final class LoginDataProvider {
    private static final String DATA_FILE = "testdata/login/login-regression.csv";

    private LoginDataProvider() {
    }

    /**
     * Hardcoded rows for quick local sanity execution.
     * Rows are filtered by @Test(description = "scenario_id").
     */
    @DataProvider(name = "loginData")
    public static Object[][] loginData(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        String rawScenarioKey = testAnnotation == null ? "" : testAnnotation.description();
        if (rawScenarioKey == null || rawScenarioKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing @Test(description=\"scenario_id\") on method: " + method.getName());
        }
        String scenarioKey = rawScenarioKey.trim();

        List<LoginCaseData> allRows = new ArrayList<>();
        allRows.add(new LoginCaseData("valid_login", "9076763805", "12345678", 1));
        allRows.add(new LoginCaseData("invalid_password", "9076763805", "Wrong@123", 1));
        allRows.add(new LoginCaseData("unregistered_phone", "0076763805", "REQUIRED", 1));

        List<LoginCaseData> selected = new ArrayList<>();
        for (LoginCaseData row : allRows) {
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
    @DataProvider(name = "loginDataCsv")
    public static Object[][] loginDataCsv(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        String rawScenarioKey = testAnnotation == null ? "" : testAnnotation.description();
        if (rawScenarioKey == null || rawScenarioKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing @Test(description=\"scenario_id\") on method: " + method.getName());
        }
        String scenarioKey = rawScenarioKey.trim();

        List<LoginCaseData> selected = new ArrayList<>();
        List<LoginCaseData> allRows = LoginCsvDataLoader.loadLoginCasesData(DATA_FILE);
        for (LoginCaseData row : allRows) {
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

    private static List<LoginCaseData> keepSingleDatasetForSmoke(Method method, List<LoginCaseData> selected) {
        if (!isSmokeMethod(method) || selected.size() <= 1) {
            return selected;
        }
        List<LoginCaseData> singleRow = new ArrayList<>();
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
