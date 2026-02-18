package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import java.util.Locale;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.registration.RegistrationDataProvider;
import tests.model.registration.RegistrationCaseData;
import tests.pages.RegistrationPage;
import tests.utils.BaseTest;
import utilities.ExtentManager;

/**
 * Data-driven registration test suite.
 */
public class RegistrationTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final Duration REGISTRATION_RESULT_TIMEOUT = Duration.ofSeconds(20);
    private static final String SCENARIO_VALID_REGISTRATION = "valid_registration";
    private static final String SCENARIO_INVALID_EMAIL = "invalid_email";
    private static final String SCENARIO_PASSWORD_MISMATCH = "password_mismatch";

    public RegistrationTest() {
        super();
    }

    public RegistrationTest(String browser) {
        super(browser);
    }

    @Test(description = "valid_registration", dataProvider = "registrationData",
            dataProviderClass = RegistrationDataProvider.class, groups = {"registration", "smoke", "regression"})
    public void registration_valid_user_should_submit(RegistrationCaseData data) {
        runRegistrationScenario(data);
    }

    @Test(description = "invalid_email", dataProvider = "registrationData",
            dataProviderClass = RegistrationDataProvider.class, groups = {"registration", "smoke", "regression"})
    public void registration_invalid_email_should_show_error(RegistrationCaseData data) {
        runRegistrationScenario(data);
    }

    @Test(description = "password_mismatch", dataProvider = "registrationData",
            dataProviderClass = RegistrationDataProvider.class, groups = {"registration", "smoke", "regression"})
    public void registration_password_mismatch_should_show_error(RegistrationCaseData data) {
        runRegistrationScenario(data);
    }

    private void runRegistrationScenario(RegistrationCaseData data) {
        ExtentTest test = extent.createTest(
                        "Registration [" + data.getDatasetId() + "] " + data.getScenarioId() + " - " + browser)
                .assignCategory("registration", data.getScenarioId(), browser);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        RegistrationPage registrationPage = new RegistrationPage(driver);

        String userType = resolveDataValue(data.getUserType());
        String name = resolveDataValue(data.getName());
        String email = resolveDynamicValue(data.getEmail(), data.getDatasetId());
        String phone = resolveDynamicValue(data.getPhone(), data.getDatasetId());
        String password = resolveDataValue(data.getPassword());
        String confirmPassword = resolveDataValue(data.getConfirmPassword());

        try {
            registrationPage.open(baseUrl);
            registrationPage.waitUntilLoaded(Duration.ofSeconds(10));
            registrationPage.fillForm(userType, name, email, phone, password, confirmPassword, data.isAgreeTerms());
            registrationPage.clickRegister();
            registrationPage.waitForOutcome(REGISTRATION_RESULT_TIMEOUT);

            if (isSuccessScenario(data.getScenarioId())) {
                Assert.assertTrue(registrationPage.isRegistrationLikelySuccessful(),
                        "Expected registration success for " + data.getDatasetId());
            } else {
                Assert.assertFalse(registrationPage.isRegistrationLikelySuccessful(),
                        "Expected registration failure for " + data.getDatasetId());
                Assert.assertTrue(registrationPage.hasNonEmptyErrorText() || registrationPage.isLoaded(),
                        "Expected validation feedback for " + data.getDatasetId());
            }

            test.pass("Scenario validated for " + data.getDatasetId())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_result"));
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating registration flow: " + timeoutException.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"));
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Validation failed: " + exception.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"));
            throw exception;
        }
    }

    private boolean isSuccessScenario(String scenarioId) {
        return SCENARIO_VALID_REGISTRATION.equals(scenarioId);
    }

    private String resolveDynamicValue(String raw, String datasetId) {
        String value = resolveDataValue(raw);
        if ("DYN_EMAIL".equalsIgnoreCase(value)) {
            return "qa+" + datasetId.toLowerCase(Locale.ROOT) + "@example.com";
        }
        if ("DYN_PHONE".equalsIgnoreCase(value)) {
            String digits = String.valueOf(Math.abs(datasetId.hashCode()));
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }
            while (digits.length() < 10) {
                digits = "0" + digits;
            }
            if (digits.startsWith("0")) {
                digits = "9" + digits.substring(1);
            }
            return digits;
        }
        return value;
    }

    private String resolveDataValue(String raw) {
        String value = safeTrim(raw);
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
