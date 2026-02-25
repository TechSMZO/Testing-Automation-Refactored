package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.io.File;
import java.time.Duration;
import org.testng.Assert;  
import org.testng.annotations.Test;
import tests.pages.LoginPage;
import tests.pages.ProfileCompletionPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * End-to-end profile completion smoke flow after login.
 */
public class ProfileCompletionTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";
    private static final String DEFAULT_PAN_NUMBER = "1111111";

    public ProfileCompletionTest() {
        super();
    }

    public ProfileCompletionTest(String browser) {
        super(browser);
    }

    @Test(description = "profile_completion_valid_flow",
            groups = {"profile", "integration", "smoke", "regression"})
    public void profile_completion_should_update_address_and_first_document() {
        ExtentTest test = extent.createTest("Profile Completion Flow - " + browser)
                .assignCategory("profile", "integration", "smoke", browser);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String username = readConfig("LOGIN_USERNAME", "login.username", DEFAULT_LOGIN_USERNAME);
        String password = readConfig("LOGIN_PASSWORD", "login.password", DEFAULT_LOGIN_PASSWORD);
        String panNumber = readConfig("PAN_NUMBER", "pan.number", DEFAULT_PAN_NUMBER);

        try {
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(baseUrl);
            loginPage.waitUntilLoaded(Duration.ofSeconds(10));
            loginPage.enterUsername(username);
            loginPage.enterPassword(password);
            loginPage.clickLogin();
            loginPage.waitForDashboard(Duration.ofSeconds(10));
            test.pass("Login successful",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "profile_flow_login_success"))
                            .build());

            ProfileCompletionPage profilePage = new ProfileCompletionPage(driver);
            profilePage.open(baseUrl);
            profilePage.waitUntilLoaded(Duration.ofSeconds(10));
            test.pass("Opened profile page",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "profile_flow_profile_page"))
                            .build());

            profilePage.openAddressEditModal();
            profilePage.fillAddressAndSave(
                    "Flat 101, Sunrise Residency",
                    "Sector 21",
                    "122001",
                    "Shipmozo QA",
                    "Shipmozo Store");
            test.pass("Address details saved",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "profile_flow_address_saved"))
                            .build());

            String uploadScreenshotRelativePath = ExtentManager.captureScreenshot(driver, "profile_flow_upload_source");
            String documentPath = new File(System.getProperty("user.dir"), "reports/" + uploadScreenshotRelativePath)
                    .getAbsolutePath();
            if (!new File(documentPath).exists()) {
                throw new RuntimeException("Upload screenshot file not found: " + documentPath);
            }

            profilePage.openDocumentsSection();
            if (isLocalExecution()) {
                profilePage.uploadPanDocument(panNumber, documentPath);
                test.pass("First document updated and uploaded",
                        MediaEntityBuilder
                                .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "profile_flow_document_saved"))
                                .build());

                Assert.assertTrue(profilePage.isPanNumberUpdated(panNumber, Duration.ofSeconds(10)),
                        "PAN number was not updated with expected value.");
            } else {
                test.info("Skipping document upload on CI/GitHub Actions environment.");
            }
            Assert.assertTrue(profilePage.isOnProfilePage(), "Expected to stay on profile page.");
            test.pass("Profile completion flow validated");
        } catch (Exception exception) {
            test.fail("Profile completion flow failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "profile_flow_failed"))
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

    private boolean isLocalExecution() {
        String ciEnv = System.getenv("CI");
        if (ciEnv != null && ciEnv.equalsIgnoreCase("true")) {
            return false;
        }
        String githubActions = System.getenv("GITHUB_ACTIONS");
        if (githubActions != null && githubActions.equalsIgnoreCase("true")) {
            return false;
        }
        return true;
    }
}
