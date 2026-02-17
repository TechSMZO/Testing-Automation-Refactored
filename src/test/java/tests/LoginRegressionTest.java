package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.login.LoginDataProvider;
import tests.model.login.LoginCaseData;
import utilities.ExtentManager;

/**
 * Data-driven login regression suite.
 */
public class LoginRegressionTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final Duration LOGIN_RESULT_TIMEOUT = Duration.ofSeconds(15);

    private static final By USERNAME_INPUT = By.id("emailphone");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[contains(text(),'Log In')]");

    public LoginRegressionTest() {
        super();
    }

    public LoginRegressionTest(String browser) {
        super(browser);
    }

    // @Test(dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
    //         groups = {"login", "smoke", "regression"})
    // public void login_valid_login_should_open_dashboard(LoginCaseData data) {
    //     runLoginScenario(data);
    // }

    // @Test(dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
    //         groups = {"login", "smoke", "regression"})
    // public void login_invalid_password_should_show_error(LoginCaseData data) {
    //     runLoginScenario(data);
    // }

    // @Test(dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
    //         groups = {"login", "smoke", "regression"})
    // public void login_unregistered_phone_should_show_error(LoginCaseData data) {
    //     runLoginScenario(data);
    // }

    private void runLoginScenario(LoginCaseData data) {
        ExtentTest test = extent.createTest(
                        "Login Regression [" + data.getTcId() + "] " + data.getDescription() + " - " + browser)
                .assignCategory("login", data.getScenarioKey(), browser);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String username = resolveDataValue(data.getUsername(), DEFAULT_LOGIN_USERNAME);
        String password = resolveDataValue(data.getPassword(), DEFAULT_LOGIN_PASSWORD);

        try {
            resetSessionState();
            driver.get(baseUrl + "/login");
            waitVisible(USERNAME_INPUT);

            type(USERNAME_INPUT, username);
            type(PASSWORD_INPUT, password);
            click(LOGIN_BUTTON);

            if ("SUCCESS".equalsIgnoreCase(data.getExpectedResult())) {
                assertSuccess(data, test);
            } else {
                assertError(data, test);
            }
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating login flow: " + timeoutException.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getTcId() + "_timeout"));
            Assert.fail("Timeout for dataset " + data.getTcId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getTcId() + "_failed"));
            throw exception;
        }
    }

    private void assertSuccess(LoginCaseData data, ExtentTest test) {
        WebDriverWait wait = new WebDriverWait(driver, LOGIN_RESULT_TIMEOUT);
        wait.until(ExpectedConditions.urlContains("/orders/new"));

        String currentUrl = driver.getCurrentUrl();
        String title = driver.getTitle();
        Assert.assertTrue(currentUrl.contains("/orders/new"),
                "Expected dashboard URL for " + data.getTcId() + " but got: " + currentUrl);
        Assert.assertTrue(title != null && title.toLowerCase(Locale.ROOT).contains("shipmozo"),
                "Expected dashboard title for " + data.getTcId() + " but got: " + title);

        test.pass("SUCCESS validated for " + data.getTcId())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getTcId() + "_success"));
    }

    private void assertError(LoginCaseData data, ExtentTest test) {
        String expectedError = safeTrim(data.getExpectedErrorText());
        WebDriverWait wait = new WebDriverWait(driver, LOGIN_RESULT_TIMEOUT);

        if (!expectedError.isEmpty()) {
            String expectedLower = expectedError.toLowerCase(Locale.ROOT);
            wait.until(driverState ->
                    driverState.getCurrentUrl().contains("/orders/new")
                            || driverState.getPageSource().toLowerCase(Locale.ROOT).contains(expectedLower));

            Assert.assertFalse(driver.getCurrentUrl().contains("/orders/new"),
                    "Unexpected successful login for " + data.getTcId());
            Assert.assertTrue(driver.getPageSource().toLowerCase(Locale.ROOT).contains(expectedLower),
                    "Expected error text not found for " + data.getTcId() + ": " + expectedError);
        } else {
            wait.until(driverState ->
                    driverState.getCurrentUrl().contains("/orders/new")
                            || !driverState.findElements(USERNAME_INPUT).isEmpty());

            Assert.assertFalse(driver.getCurrentUrl().contains("/orders/new"),
                    "Expected login rejection for " + data.getTcId());
        }

        test.pass("ERROR validated for " + data.getTcId())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getTcId() + "_error"));
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
