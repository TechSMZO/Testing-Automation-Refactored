package tests.data.rate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.model.rate.RateCalculatorCaseData;

/**
 * Provides rate-calculator datasets for TestNG.
 * Use `rateCalculatorData` for quick hardcoded runs and `rateCalculatorDataCsv` for CSV-driven runs.
 */
public final class RateCalculatorDataProvider {
    private static final String DATA_FILE = "testdata/rate/rate-calculator-regression.csv";

    private RateCalculatorDataProvider() {
    }

    /**
     * Hardcoded rows for quick local execution.
     * Rows are filtered by @Test(description = "scenario_id").
     */
    @DataProvider(name = "rateCalculatorData")
    public static Object[][] rateCalculatorData(Method method) {
        String scenarioKey = extractScenarioKey(method);

        List<RateCalculatorCaseData> allRows = new ArrayList<>();
        allRows.add(new RateCalculatorCaseData(
                "rate_calculator_valid_flow",
                "domestic",
                "single package (b2c)",
                "400012",
                "122001",
                "0.5",
                "100",
                "10",
                "10",
                "10",
                true,
                1));
        allRows.add(new RateCalculatorCaseData(
                "rate_calculator_domestic_b2b_flow",
                "domestic",
                "single package (b2b)",
                "400012",
                "122001",
                "0.5",
                "100",
                "10",
                "10",
                "10",
                true,
                1));
        allRows.add(new RateCalculatorCaseData(
                "rate_calculator_international_flow",
                "international",
                "single package",
                "400012",
                "US10001",
                "0.5",
                "100",
                "10",
                "10",
                "10",
                true,
                1));
        return filterRows(method, scenarioKey, allRows);
    }

    /**
     * CSV rows filtered by scenario_id from @Test(description = "...").
     */
    @DataProvider(name = "rateCalculatorDataCsv")
    public static Object[][] rateCalculatorDataCsv(Method method) {
        String scenarioKey = extractScenarioKey(method);
        List<RateCalculatorCaseData> allRows =
                RateCalculatorCsvDataLoader.loadRateCalculatorCasesData(DATA_FILE);
        return filterRows(method, scenarioKey, allRows);
    }

    private static Object[][] filterRows(Method method, String scenarioKey, List<RateCalculatorCaseData> allRows) {
        List<RateCalculatorCaseData> selected = new ArrayList<>();
        for (RateCalculatorCaseData row : allRows) {
            if (!scenarioKey.equals(row.getScenarioId())) {
                continue;
            }
            if (!row.isRunEnabled()) {
                continue;
            }
            selected.add(row);
        }
        selected = keepSingleDatasetForSmoke(method, selected);

        if (selected.isEmpty()) {
            throw new SkipException("No enabled rows found for scenario '" + scenarioKey + "'");
        }

        Object[][] rows = new Object[selected.size()][1];
        for (int index = 0; index < selected.size(); index++) {
            rows[index][0] = selected.get(index);
        }
        return rows;
    }

    private static String extractScenarioKey(Method method) {
        Test testAnnotation = method.getAnnotation(Test.class);
        String rawScenarioKey = testAnnotation == null ? "" : testAnnotation.description();
        if (rawScenarioKey == null || rawScenarioKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing @Test(description=\"scenario_id\") on method: " + method.getName());
        }
        return rawScenarioKey.trim();
    }

    private static List<RateCalculatorCaseData> keepSingleDatasetForSmoke(
            Method method, List<RateCalculatorCaseData> selected) {
        if (!isSmokeMethod(method) || selected.size() <= 1) {
            return selected;
        }
        List<RateCalculatorCaseData> singleRow = new ArrayList<>();
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
