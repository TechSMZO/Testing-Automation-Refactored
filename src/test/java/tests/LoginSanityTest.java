package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.time.Duration;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.pages.LoginPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * Focused login validation test.
 */
public class LoginSanityTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final Duration LOGIN_RESULT_TIMEOUT = Duration.ofSeconds(10);

    public LoginSanityTest() {
        super();
    }

    public LoginSanityTest(String browser) {
        super(browser);
    }
   
    @Test(groups = {"sanity"}, priority = 1)
    public void userCanLoginWithValidCredentials() {
        ExtentTest test = extent.createTest("Login Demo Test - " + browser).assignCategory(browser);
        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String loginUsername = readConfig("LOGIN_USERNAME", "login.username", DEFAULT_LOGIN_USERNAME);
        String loginPassword = readConfig("LOGIN_PASSWORD", "login.password", DEFAULT_LOGIN_PASSWORD);
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open(baseUrl);
        loginPage.waitUntilLoaded(Duration.ofSeconds(10));
        test.pass("Opened Login Page",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_page"))
                        .build());
        if (!loginPage.isLoaded()) {
            test.fail("Failed to load login page on " + browser);
            Assert.fail("Login page not loaded.");
        }

        try {
            long startTime = System.currentTimeMillis();
            loginPage.enterUsername(loginUsername);
            test.pass("Entered Username",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "username_entered"))
                            .build());

            loginPage.enterPassword(loginPassword);
            test.pass("Entered Password",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "password_entered"))
                            .build());

            loginPage.clickLogin();
            test.pass("Clicked Login",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "logging_in"))
                            .build());

            loginPage.waitForDashboard(LOGIN_RESULT_TIMEOUT);
            Assert.assertTrue(loginPage.isDashboardLoaded(), "Dashboard did not load after login.");

            long durationMillis = System.currentTimeMillis() - startTime;
            double durationSeconds = durationMillis / 1000.0;
            System.out.println("Login to Dashboard Load Time: " + durationSeconds + " seconds");
            test.info("Login to Dashboard Load Time: " + durationSeconds + " seconds");
            test.pass("Successfully landed on dashboard",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_success"))
                            .build());
        } catch (TimeoutException exception) {
            test.fail("Login failed or took too long",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"))
                            .build());
            Assert.fail("Login validation failed due to timeout.");
        } catch (Exception exception) {
            test.fail("Login validation failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"))
                            .build());
            throw exception;
        }
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
