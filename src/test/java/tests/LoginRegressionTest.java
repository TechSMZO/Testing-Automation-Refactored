package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.login.LoginDataProvider;
import tests.model.login.LoginCaseData;
import tests.pages.LoginPage;
import tests.utils.BaseTest;
import utilities.ExtentManager;

/**
 * Data-driven login regression suite.
 */
public class LoginRegressionTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final Duration LOGIN_RESULT_TIMEOUT = Duration.ofSeconds(15);
    private static final String SCENARIO_VALID_LOGIN = "valid_login";
    private static final String SCENARIO_INVALID_PASSWORD = "invalid_password";
    private static final String SCENARIO_UNREGISTERED_PHONE = "unregistered_phone";

    public LoginRegressionTest() {
        super();
    }

    public LoginRegressionTest(String browser) {
        super(browser);
    }

    @Test(description = "valid_login", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "smoke", "regression"})
    public void login_valid_login_should_open_dashboard(LoginCaseData data) {
        runLoginScenario(data);
    }

    @Test(description = "invalid_password", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "smoke", "regression"})
    public void login_invalid_password_should_show_error(LoginCaseData data) {
        runLoginScenario(data);
    }

    @Test(description = "unregistered_phone", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "smoke", "regression"})
    public void login_unregistered_phone_should_show_error(LoginCaseData data) {
        runLoginScenario(data);
    }

    private void runLoginScenario(LoginCaseData data) {
        ExtentTest test = extent.createTest(
                        "Login Regression [" + data.getDatasetId() + "] " + data.getScenarioId() + " - " + browser)
                .assignCategory("login", data.getScenarioId(), browser);
        LoginPage loginPage = new LoginPage(driver);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String username = resolveDataValue(data.getUsername(), DEFAULT_LOGIN_USERNAME);
        String password = resolveDataValue(data.getPassword(), DEFAULT_LOGIN_PASSWORD);

        try {
            resetSessionState();
            loginPage.open(baseUrl);
            loginPage.waitUntilLoaded(Duration.ofSeconds(10));
            loginPage.login(username, password);

            if (isSuccessScenario(data.getScenarioId())) {
                assertSuccess(data, test, loginPage);
            } else {
                assertError(data, test, loginPage);
            }
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating login flow: " + timeoutException.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"));
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"));
            throw exception;
        }
    }

    private void assertSuccess(LoginCaseData data, ExtentTest test, LoginPage loginPage) {
        loginPage.waitForDashboard(LOGIN_RESULT_TIMEOUT);
        Assert.assertTrue(loginPage.isDashboardLoaded(),
                "Expected dashboard loaded state for " + data.getDatasetId());

        test.pass("SUCCESS validated for " + data.getDatasetId())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_success"));
    }

    private void assertError(LoginCaseData data, ExtentTest test, LoginPage loginPage) {
        boolean isErrorVisible = isExpectedErrorVisible(data.getScenarioId(), loginPage);
        Assert.assertFalse(loginPage.isDashboardLoaded(),
                "Unexpected successful login for " + data.getDatasetId());
        Assert.assertTrue(isErrorVisible || loginPage.isLoginFormVisible(),
                "Expected login rejection state for " + data.getDatasetId());

        test.pass("ERROR validated for " + data.getDatasetId())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_error"));
    }

    private boolean isSuccessScenario(String scenarioId) {
        return SCENARIO_VALID_LOGIN.equals(scenarioId);
    }

    private boolean isExpectedErrorVisible(String scenarioId, LoginPage loginPage) {
        switch (scenarioId) {
            case SCENARIO_INVALID_PASSWORD:
                return loginPage.isInvalidCredentialsVisible(LOGIN_RESULT_TIMEOUT);
            case SCENARIO_UNREGISTERED_PHONE:
                return loginPage.isUnregisteredPhoneVisible(LOGIN_RESULT_TIMEOUT);
            default:
                throw new IllegalArgumentException("No expected behavior mapping for scenario_id: " + scenarioId);
        }
    }

    private void resetSessionState() {
        driver.manage().deleteAllCookies();
        if (driver instanceof JavascriptExecutor) {
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("window.localStorage.clear();");
                js.executeScript("window.sessionStorage.clear();");
            } catch (RuntimeException ignored) {
                // Browser may block storage access on some blank states. Safe to ignore.
            }
        }
    }

    private String resolveDataValue(String value, String fallback) {
        String trimmed = safeTrim(value);
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.startsWith("ENV:")) {
            String key = trimmed.substring(4).trim();
            if (key.isEmpty()) {
                return fallback;
            }

            String envValue = System.getenv(key);
            if (envValue != null && !envValue.isBlank()) {
                return envValue.trim();
            }

            String exactProperty = System.getProperty(key);
            if (exactProperty != null && !exactProperty.isBlank()) {
                return exactProperty.trim();
            }

            String dottedProperty = System.getProperty(key.toLowerCase(Locale.ROOT).replace('_', '.'));
            if (dottedProperty != null && !dottedProperty.isBlank()) {
                return dottedProperty.trim();
            }
            return fallback;
        }
        return trimmed;
    }

    private String readConfig(String envKey, String propertyKey, String fallback) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        return fallback;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
