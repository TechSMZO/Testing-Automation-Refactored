package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.registration.RegistrationDataProvider;
import tests.model.registration.RegistrationCaseData;
import tests.pages.RegistrationPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * Data-driven registration test suite.
 */
public class RegistrationTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.appiify.com/";
    private static final Duration REGISTRATION_RESULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String REGISTRATION_OTP = "111111";

    public RegistrationTest() {
        super();
    }

    public RegistrationTest(String browser) {
        super(browser);
    }

    @Test(description = "valid_registration", dataProvider = "registrationDataCsv",
            dataProviderClass = RegistrationDataProvider.class,
            groups = {"registration", "registration-smoke", "smoke", "regression"})
    public void registration_valid_user_should_submit(RegistrationCaseData data) {
        ExtentTest test = extent.createTest(
                        "Registration [" + data.getDatasetId() + "] valid_registration - " + browser)
                .assignCategory("registration", "valid_registration", browser);
        try {
            RegistrationPage registrationPage = submitRegistrationForm(data, test);
            registrationPage.waitForOutcome(REGISTRATION_RESULT_TIMEOUT);

            Assert.assertTrue(registrationPage.isRegistrationLikelySuccessful(),
                    "Expected registration success for " + data.getDatasetId());

            test.pass("SUCCESS validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_result"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating registration flow: " + timeoutException.getMessage(),
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

    @Test(description = "invalid_email", dataProvider = "registrationDataCsv",
            dataProviderClass = RegistrationDataProvider.class, groups = {"registration", "regression"})
    public void registration_invalid_email_should_show_error(RegistrationCaseData data) {
        ExtentTest test = extent.createTest(
                        "Registration [" + data.getDatasetId() + "] invalid_email - " + browser)
                .assignCategory("registration", "invalid_email", browser);
        try {
            RegistrationPage registrationPage = submitRegistrationForm(data, test);
            registrationPage.waitForOutcome(REGISTRATION_RESULT_TIMEOUT);

            Assert.assertFalse(registrationPage.isRegistrationLikelySuccessful(),
                    "Expected registration failure for " + data.getDatasetId());
            Assert.assertTrue(registrationPage.hasNonEmptyErrorText() || registrationPage.isLoaded(),
                    "Expected validation feedback for " + data.getDatasetId());

            test.pass("ERROR validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_result"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating registration flow: " + timeoutException.getMessage(),
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

    @Test(description = "password_mismatch", dataProvider = "registrationDataCsv",
            dataProviderClass = RegistrationDataProvider.class, groups = {"registration", "regression"})
    public void registration_password_mismatch_should_show_error(RegistrationCaseData data) {
        ExtentTest test = extent.createTest(
                        "Registration [" + data.getDatasetId() + "] password_mismatch - " + browser)
                .assignCategory("registration", "password_mismatch", browser);
        try {
            RegistrationPage registrationPage = submitRegistrationForm(data, test);
            registrationPage.waitForOutcome(REGISTRATION_RESULT_TIMEOUT);

            Assert.assertFalse(registrationPage.isRegistrationLikelySuccessful(),
                    "Expected registration failure for " + data.getDatasetId());
            Assert.assertTrue(registrationPage.hasNonEmptyErrorText() || registrationPage.isLoaded(),
                    "Expected validation feedback for " + data.getDatasetId());

            test.pass("ERROR validated for " + data.getDatasetId(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_result"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while validating registration flow: " + timeoutException.getMessage(),
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
     * Shared form-fill and submit flow reused by all registration scenarios.
     */
    private RegistrationPage submitRegistrationForm(RegistrationCaseData data, ExtentTest test) {
        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        RegistrationPage registrationPage = new RegistrationPage(driver);

        String userType = resolveDataValue(data.getUserType());
        String scenarioId = data.getScenarioId();
        String name = resolveDataValue(data.getName());
        // String email = generateRuntimeEmail(scenarioId, data.getDatasetId());
        // String phone = generateRuntimePhone();
        String email = generateRuntimeEmail(scenarioId, data.getDatasetId());
        String phone = generateRuntimePhone();
        String password = resolveDataValue(data.getPassword());
        String confirmPassword = resolveDataValue(data.getConfirmPassword());

        registrationPage.open(baseUrl);
        registrationPage.waitUntilLoaded(Duration.ofSeconds(10));
        test.pass("Opened registration page",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_open"))
                        .build());

        registrationPage.selectUserType(userType);
        test.pass("Selected user type",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_user_type"))
                        .build());

        registrationPage.typeName(name);
        test.pass("Entered name",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_name"))
                        .build());

        registrationPage.typeEmail(email);
        test.pass("Entered email",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_email"))
                        .build());

        registrationPage.typePhone(phone);
        test.pass("Entered phone",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_phone"))
                        .build());

        registrationPage.clickVerifyPhone();
        test.pass("Clicked phone verify",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_verify_phone"))
                        .build());

        registrationPage.waitForOtpPopup(Duration.ofSeconds(10));
        registrationPage.enterOtp(REGISTRATION_OTP);
        test.pass("Entered OTP",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_otp"))
                        .build());

        registrationPage.clickVerifyAndContinue();
        test.pass("Clicked verify and continue",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_verify_continue"))
                        .build());

        registrationPage.typePassword(password);
        test.pass("Entered password",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_password"))
                        .build());

        registrationPage.typeConfirmPassword(confirmPassword);
        test.pass("Entered confirm password",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_confirm_password"))
                        .build());

        registrationPage.setAgreeTerms(data.isAgreeTerms());
        test.pass("Set agree terms",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_terms"))
                        .build());

        registrationPage.clickRegister();
        test.pass("Clicked register",
                MediaEntityBuilder
                        .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_register"))
                        .build());

        return registrationPage;
    }

    /**
     * Always generates email at runtime and ignores CSV value.
     * For invalid_email scenario it still returns an invalid format email by design.
     */
    private String generateRuntimeEmail(String scenarioId, String datasetId) {
        String token = System.currentTimeMillis() + String.valueOf(ThreadLocalRandom.current().nextInt(100, 999));
        if ("invalid_email".equalsIgnoreCase(scenarioId)) {
            return "invalid_" + datasetId.toLowerCase() + "_" + token;
        }
        return "qa+" + datasetId.toLowerCase() + "_" + token + "@example.com";
    }

    /**
     * Always generates a unique 10-digit Indian-style phone number at runtime and ignores CSV value.
     */
    private String generateRuntimePhone() {
        String digits = String.valueOf(System.currentTimeMillis())
                + ThreadLocalRandom.current().nextInt(100, 999);
        if (digits.length() > 9) {
            digits = digits.substring(digits.length() - 9);
        }
        while (digits.length() < 9) {
            digits = "0" + digits;
        }
        return "9" + digits;
    }

    /**
     * Resolves values in this order: plain value, ENV:key from env var/system property.
     */
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
