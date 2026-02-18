package tests.data.login;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.SkipException;
import tests.model.login.LoginCaseData;

/**
 * Shared dataset provider for all login regression scenarios.
 */
public final class LoginDataProvider {
    private static final String DATA_FILE = "testdata/login/login-regression.csv";

    private LoginDataProvider() {
    }

    @DataProvider(name = "loginData")
    public static Object[][] loginData() {

        return new Object[][]{

            {new LoginCaseData("valid_login","9076763805", "12345678", 1)},

            {new LoginCaseData("invalid_password","9076763805", "Wrong@123", 1)},

            {new LoginCaseData("unregistered_phone", "0076763805", "REQUIRED", 1)}
        };
    }

    @DataProvider(name = "loginDataCsv")
    public static Object[][] loginData(Method method) {
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

        if (selected.isEmpty()) {
            throw new SkipException("No rows found for scenario '" + scenarioKey + "'");
        }

        Object[][] rows = new Object[selected.size()][1];
        for (int index = 0; index < selected.size(); index++) {
            rows[index][0] = selected.get(index);
        }
        return rows;
    }
}
