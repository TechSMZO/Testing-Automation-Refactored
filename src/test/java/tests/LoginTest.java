package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExtentManager;

/**
 * Focused login validation test.
 */
public class LoginTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final Duration LOGIN_RESULT_TIMEOUT = Duration.ofSeconds(15);

    public LoginTest() {
        super();
    }

    public LoginTest(String browser) {
        super(browser);
    }

    @Test(groups = {"smoke"}, priority = 1)
    public void userCanLoginWithValidCredentials() {
        ExtentTest test = extent.createTest("Login Demo Test - " + browser).assignCategory(browser);
        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String loginUsername = readConfig("LOGIN_USERNAME", "login.username", DEFAULT_LOGIN_USERNAME);
        String loginPassword = readConfig("LOGIN_PASSWORD", "login.password", DEFAULT_LOGIN_PASSWORD);

        driver.get(baseUrl + "/login");
        test.pass("Opened Login Page");
        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_page"));
        if (!driver.getCurrentUrl().contains("login")) {
            test.fail("Failed to load login page on " + browser);
            Assert.fail("Login page not loaded.");
        }

        try {
            type(By.id("emailphone"), loginUsername);
            test.pass("Entered Username");
        } catch (Exception exception) {
            test.fail("Failed to enter username: " + exception.getMessage());
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "username_entry_error"));
            throw new RuntimeException("Unable to enter username", exception);
        }

        type(By.id("password"), loginPassword);
        test.pass("Entered Password");
        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "credentials_entered"));
        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "logging_in"));

        long startTime = System.currentTimeMillis();
        click(By.xpath("//button[contains(text(),'Log In')]"));
        try {
            WebDriverWait wait = new WebDriverWait(driver, LOGIN_RESULT_TIMEOUT);
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/orders/new"),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Invalid credentials')]")),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[contains(text(),'Email or phone is not registered.')]"))
            ));

            String currentUrl = driver.getCurrentUrl();
            String title = driver.getTitle();
            if (currentUrl.contains("/orders/new") && title.contains("Shipmozo")) {
                test.pass("Successfully landed on dashboard");
                long durationMillis = System.currentTimeMillis() - startTime;
                double durationSeconds = durationMillis / 1000.0;
                System.out.println("Login to Dashboard Load Time: " + durationSeconds + " seconds");
                test.info("Login to Dashboard Load Time: " + durationSeconds + " seconds");
                ExtentManager.wait(5);
                test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_success"));
            } else {
                test.fail("Login Failed: Either wrong credentials or login error occurred")
                        .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"));
                Assert.fail("Login Failed");
            }
        } catch (TimeoutException exception) {
            test.fail("Login failed or took too long")
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"));
            Assert.fail("Login validation failed due to timeout.");
        }
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
}
