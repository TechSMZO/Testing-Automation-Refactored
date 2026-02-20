package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.rate.RateCalculatorDataProvider;
import tests.model.rate.RateCalculatorCaseData;
import tests.pages.LoginPage;
import tests.pages.RateCalculatorPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * Data-driven rate calculator suite.
 */
public class RateCalculatorTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";

    public RateCalculatorTest() {
        super();
    }

    public RateCalculatorTest(String browser) {
        super(browser);
    }

    @Test(description = "rate_calculator_valid_flow", dataProvider = "rateCalculatorDataCsv",
            dataProviderClass = RateCalculatorDataProvider.class, groups = {"rate-calculator", "smoke", "regression"})
    public void rate_calculator_valid_flow_should_show_courier_rates(RateCalculatorCaseData data) {
        executeRateCalculationFlow(data, "rate_calculator_valid_flow", true);
    }

    @Test(description = "rate_calculator_domestic_b2b_flow", dataProvider = "rateCalculatorDataCsv",
            dataProviderClass = RateCalculatorDataProvider.class, groups = {"rate-calculator", "regression"})
    public void rate_calculator_domestic_b2b_flow_should_show_courier_rates(RateCalculatorCaseData data) {
        executeRateCalculationFlow(data, "rate_calculator_domestic_b2b_flow", true);
    }

    @Test(description = "rate_calculator_international_flow", dataProvider = "rateCalculatorDataCsv",
            dataProviderClass = RateCalculatorDataProvider.class, groups = {"rate-calculator", "regression"})
    public void rate_calculator_international_flow_should_show_courier_rates(RateCalculatorCaseData data) {
        executeRateCalculationFlow(data, "rate_calculator_international_flow", false);
    }

    private void executeRateCalculationFlow(RateCalculatorCaseData data, String scenarioName, boolean assertPackageType) {
        ExtentTest test = extent.createTest(
                        "Rate Calculator [" + data.getDatasetId() + "] " + scenarioName + " - " + browser)
                .assignCategory("rate-calculator", scenarioName, browser);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String loginUsername = readConfig("LOGIN_USERNAME", "login.username", DEFAULT_LOGIN_USERNAME);
        String loginPassword = readConfig("LOGIN_PASSWORD", "login.password", DEFAULT_LOGIN_PASSWORD);

        try {
            resetSessionState();
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(baseUrl);
            loginPage.waitUntilLoaded(Duration.ofSeconds(10));
            loginPage.enterUsername(loginUsername);
            loginPage.enterPassword(loginPassword);
            loginPage.clickLogin();
            loginPage.waitForDashboard(Duration.ofSeconds(10));
            test.pass("Login successful",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_login"))
                            .build());

            RateCalculatorPage rateCalculatorPage = new RateCalculatorPage(driver);
            rateCalculatorPage.open(baseUrl);
            rateCalculatorPage.waitUntilLoaded(Duration.ofSeconds(10));
            test.pass("Opened rate calculator page",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_open"))
                            .build());

            rateCalculatorPage.selectCalculatorMode(resolveDataValue(data.getCalculatorMode()));
            test.pass("Selected calculator mode: " + resolveDataValue(data.getCalculatorMode()),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_mode"))
                            .build());

            boolean packageSelected = rateCalculatorPage.trySelectPackageType(resolveDataValue(data.getPackageType()));
            String packageValue = rateCalculatorPage.capturePackageTypeValue().toLowerCase();
            if (assertPackageType) {
                String expectedPackage = resolveDataValue(data.getPackageType()).toLowerCase();
                String expectedToken = expectedPackage.contains("b2b") ? "b2b"
                        : expectedPackage.contains("b2c") ? "b2c" : expectedPackage;
                Assert.assertTrue((packageSelected || !packageValue.isBlank()) && packageValue.contains(expectedToken),
                        "Expected package type selection for dataset " + data.getDatasetId()
                                + ". Expected: " + expectedPackage + ", Visible: " + packageValue);
            }
            test.pass("Handled package type: " + resolveDataValue(data.getPackageType()),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_package_type"))
                            .build());

            rateCalculatorPage.fillForm(
                    resolveDataValue(data.getOriginPincode()),
                    resolveDataValue(data.getDestinationPincode()),
                    resolveDataValue(data.getWeight()),
                    resolveDataValue(data.getInvoiceValue()),
                    resolveDataValue(data.getLength()),
                    resolveDataValue(data.getWidth()),
                    resolveDataValue(data.getHeight()));
            scrollToBottom();
            test.pass("Filled rate calculator form",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_form"))
                            .build());

            rateCalculatorPage.clickCalculate();
            scrollToBottom();
            test.pass("Clicked calculate",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_calculate"))
                            .build());

            boolean hasResults = rateCalculatorPage.waitForResults(Duration.ofSeconds(10));
            String validationText = rateCalculatorPage.captureVisibleValidationText();
            Assert.assertTrue(hasResults,
                    "Expected courier rates after calculate. Validation/Status: "
                            + (validationText.isBlank() ? "No visible validation text." : validationText));

            scrollToBottom();
            test.pass("Rate calculator results loaded",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_result"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating rate calculator flow: " + timeoutException.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"))
                            .build());
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Rate calculator flow failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"))
                            .build());
            throw exception;
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
                // Some blank/browser states can block storage access.
            }
        }
    }

    private void scrollToBottom() {
        if (!(driver instanceof JavascriptExecutor)) {
            return;
        }
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            ExtentManager.wait(1);
        } catch (RuntimeException ignored) {
            // Ignore scroll failure and continue the flow.
        }
    }

    private String resolveDataValue(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.startsWith("ENV:")) {
            String key = value.substring(4).trim();
            if (key.isEmpty()) {
                return "";
            }
            String envValue = System.getenv(key);
            if (envValue != null && !envValue.isBlank()) {
                return envValue.trim();
            }
            String sysValue = System.getProperty(key);
            if (sysValue != null && !sysValue.isBlank()) {
                return sysValue.trim();
            }
            return "";
        }
        return value;
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
