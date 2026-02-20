package tests.data.order;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import tests.model.order.AddOrderCaseData;

/**
 * Provides add-order datasets for TestNG.
 * Use `addOrderData` for quick hardcoded runs and `addOrderDataCsv` for CSV-driven runs.
 */
public final class AddOrderDataProvider {
    private static final String DATA_FILE = "testdata/order/add-order-regression.csv";

    private AddOrderDataProvider() {
    }

    /**
     * Hardcoded rows for quick local sanity execution.
     * Rows are filtered by @Test(description = "scenario_id").
     */
    @DataProvider(name = "addOrderData")
    public static Object[][] addOrderData(Method method) {
        String scenarioKey = extractScenarioKey(method);

        List<AddOrderCaseData> allRows = new ArrayList<>();
        allRows.add(new AddOrderCaseData(
                "add_order_b2c_e2e",
                "QA Buyer",
                "DYN_PHONE",
                "",
                "DYN_EMAIL",
                "",
                "Flat 101, Sunrise Residency",
                "Sector 21",
                "122001",
                "",
                "essential",
                "Test Product",
                "1",
                "100",
                "prepaid",
                "",
                "",
                "0",
                "0.5",
                "10",
                "10",
                "10",
                "",
                "",
                "",
                "QA Warehouse",
                "DYN_PHONE",
                "warehouse.qa@example.com",
                true,
                1));

        return filterRows(method, scenarioKey, allRows);
    }

    /**
     * CSV rows filtered by scenario_id from @Test(description = "...").
     */
    @DataProvider(name = "addOrderDataCsv")
    public static Object[][] addOrderDataCsv(Method method) {
        String scenarioKey = extractScenarioKey(method);
        List<AddOrderCaseData> allRows = AddOrderCsvDataLoader.loadAddOrderCasesData(DATA_FILE);
        return filterRows(method, scenarioKey, allRows);
    }

    private static Object[][] filterRows(Method method, String scenarioKey, List<AddOrderCaseData> allRows) {
        List<AddOrderCaseData> selected = new ArrayList<>();
        for (AddOrderCaseData row : allRows) {
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

    private static List<AddOrderCaseData> keepSingleDatasetForSmoke(Method method, List<AddOrderCaseData> selected) {
        if (!isSmokeMethod(method) || selected.size() <= 1) {
            return selected;
        }
        List<AddOrderCaseData> singleRow = new ArrayList<>();
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
