package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.login.LoginDataProvider;
import tests.model.login.LoginCaseData;
import tests.pages.LoginPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * Data-driven login regression suite.
 */
public class LoginTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final Duration LOGIN_RESULT_TIMEOUT = Duration.ofSeconds(10);

    public LoginTest() {
        super();
    }

    public LoginTest(String browser) {
        super(browser);
    }

    @Test(description = "valid_login", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "smoke", "regression"})
    public void login_valid_login_should_open_dashboard(LoginCaseData data) {
        ExtentTest test = extent.createTest(
                        "Login Regression [" + data.getDatasetId() + "] valid_login - " + browser)
                .assignCategory("login", "valid_login", browser);
        try {
            LoginPage loginPage = loginWithData(data, test);
            loginPage.waitForDashboard(LOGIN_RESULT_TIMEOUT);
            Assert.assertTrue(loginPage.isDashboardLoaded(),
                    "Expected dashboard loaded state for " + data.getDatasetId());

            test.pass("SUCCESS validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_success"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating login flow: " + timeoutException.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"))
                            .build());
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"))
                            .build());
            throw exception;
        }
    }

    @Test(description = "invalid_password", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "regression"})
    public void login_invalid_password_should_show_error(LoginCaseData data) {
        ExtentTest test = extent.createTest(
                        "Login Regression [" + data.getDatasetId() + "] invalid_password - " + browser)
                .assignCategory("login", "invalid_password", browser);
        try {
            LoginPage loginPage = loginWithData(data, test);
            boolean isErrorVisible = loginPage.isInvalidCredentialsVisible(LOGIN_RESULT_TIMEOUT);

            Assert.assertFalse(loginPage.isDashboardLoaded(),
                    "Unexpected successful login for " + data.getDatasetId());
            Assert.assertTrue(isErrorVisible || loginPage.isLoginFormVisible(),
                    "Expected invalid password error for " + data.getDatasetId());

            test.pass("ERROR validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_error"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating login flow: " + timeoutException.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"))
                            .build());
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"))
                            .build());
            throw exception;
        }
    }

    @Test(description = "unregistered_phone", dataProvider = "loginData", dataProviderClass = LoginDataProvider.class,
            groups = {"login", "regression"})
    public void login_unregistered_phone_should_show_error(LoginCaseData data) {
        ExtentTest test = extent.createTest(
                        "Login Regression [" + data.getDatasetId() + "] unregistered_phone - " + browser)
                .assignCategory("login", "unregistered_phone", browser);
        try {
            LoginPage loginPage = loginWithData(data, test);
            boolean isErrorVisible = loginPage.isUnregisteredPhoneVisible(LOGIN_RESULT_TIMEOUT);

            Assert.assertFalse(loginPage.isDashboardLoaded(),
                    "Unexpected successful login for " + data.getDatasetId());
            Assert.assertTrue(isErrorVisible || loginPage.isLoginFormVisible(),
                    "Expected unregistered phone/email error for " + data.getDatasetId());

            test.pass("ERROR validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_error"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating login flow: " + timeoutException.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"))
                            .build());
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"))
                            .build());
            throw exception;
        }
    }

    /**
     * Common login steps reused by all login scenarios.
     */
    private LoginPage loginWithData(LoginCaseData data, ExtentTest test) {
        LoginPage loginPage = new LoginPage(driver);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String username = resolveDataValue(data.getUsername(), DEFAULT_LOGIN_USERNAME);
        String password = resolveDataValue(data.getPassword(), DEFAULT_LOGIN_PASSWORD);

        resetSessionState();
        loginPage.open(baseUrl);
        loginPage.waitUntilLoaded(Duration.ofSeconds(10));

        loginPage.enterUsername(username);
        test.pass("Entered username",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_username"))
                        .build());

        loginPage.enterPassword(password);
        test.pass("Entered password",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_password"))
                        .build());

        loginPage.clickLogin();
        test.pass("Clicked login",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_login_click"))
                        .build());

        return loginPage;
    }

    /**
     * Clears browser state so each dataset starts from a clean session.
     */
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

    /**
     * Resolves values in this order: plain value, ENV:key from env var/system property, then fallback.
     */
    private String resolveDataValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
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
            return fallback;
        }
        return trimmed;
    }

    /**
     * Config precedence: environment variable -> JVM system property -> fallback.
     */
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
}
