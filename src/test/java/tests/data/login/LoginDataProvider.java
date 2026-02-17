package tests.data.login;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import tests.model.login.LoginCaseData;

/**
 * Shared dataset provider for all login regression scenarios.
 */
public final class LoginDataProvider {
    private static final String DATA_FILE = "testdata/login/login-regression.csv";

    private static final Map<String, String> METHOD_TO_SCENARIO = Map.of(
            "login_valid_login_should_open_dashboard", "valid_login",
            "login_invalid_password_should_show_error", "invalid_password",
            "login_unregistered_phone_should_show_error", "unregistered_phone"
    );

    private static volatile List<LoginCaseData> cachedData;

    private LoginDataProvider() {
    }

    @DataProvider(name = "loginData")
    public static Object[][] loginData(Method method) {
        String scenarioKey = METHOD_TO_SCENARIO.get(method.getName());
        if (scenarioKey == null) {
            throw new IllegalArgumentException("No scenario mapping for test method: " + method.getName());
        }

        String suiteType = resolveSuiteType();
        List<LoginCaseData> selected = getDataRows().stream()
                .filter(LoginCaseData::isEnabled)
                .filter(data -> scenarioKey.equals(data.getScenarioKey()))
                .filter(data -> includeForSuite(data, suiteType))
                .sorted(Comparator.comparingInt(LoginCaseData::getPriority).thenComparing(LoginCaseData::getTcId))
                .toList();

        if (selected.isEmpty()) {
            throw new SkipException("No enabled rows found for scenario '" + scenarioKey + "' and suite.type='"
                    + suiteType + "'");
        }

        Object[][] rows = new Object[selected.size()][1];
        for (int index = 0; index < selected.size(); index++) {
            rows[index][0] = selected.get(index);
        }
        return rows;
    }

    private static List<LoginCaseData> getDataRows() {
        List<LoginCaseData> localData = cachedData;
        if (localData == null) {
            synchronized (LoginDataProvider.class) {
                localData = cachedData;
                if (localData == null) {
                    localData = CsvDataLoader.loadLoginCases(DATA_FILE);
                    cachedData = localData;
                }
            }
        }
        return localData;
    }

    private static boolean includeForSuite(LoginCaseData data, String suiteType) {
        switch (suiteType) {
            case "smoke":
                return data.isRunSmoke();
            case "regression":
                return data.isRunRegression();
            case "all":
                return data.isRunSmoke() || data.isRunRegression();
            default:
                return data.isRunRegression();
        }
    }

    private static String resolveSuiteType() {
        String raw = System.getProperty("suite.type");
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("SUITE_TYPE");
        }
        if (raw == null || raw.isBlank()) {
            return "regression";
        }
        return raw.trim().toLowerCase();
    }
}
