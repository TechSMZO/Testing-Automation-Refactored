package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.data.order.AddOrderDataProvider;
import tests.model.order.AddOrderCaseData;
import tests.pages.AddOrderPage;
import tests.pages.LoginPage;
import tests.utils.BaseTest;
import tests.utils.ExtentManager;

/**
 * Data-driven Add Order B2C end-to-end flow until courier assignment.
 */
public class AddOrderTest extends BaseTest {
    private static final String DEFAULT_BASE_URL = "https://panel.shipmozo.com";
    private static final String DEFAULT_LOGIN_USERNAME = "9076763805";
    private static final String DEFAULT_LOGIN_PASSWORD = "12345678";

    public AddOrderTest() {
        super();
    }

    public AddOrderTest(String browser) {
        super(browser);
    }

    @Test(description = "add_order_b2c_e2e", dataProvider = "addOrderDataCsv",
            dataProviderClass = AddOrderDataProvider.class, groups = {"add-order", "smoke", "regression"})
    public void add_order_b2c_e2e_should_create_order_and_assign_courier(AddOrderCaseData data) {
        ExtentTest test = extent.createTest(
                        "Add Order [" + data.getDatasetId() + "] add_order_b2c_e2e - " + browser)
                .assignCategory("add-order", "add_order_b2c_e2e", browser);

        String baseUrl = readConfig("BASE_URL", "base.url", DEFAULT_BASE_URL);
        String loginUsername = readConfig("LOGIN_USERNAME", "login.username", DEFAULT_LOGIN_USERNAME);
        String loginPassword = readConfig("LOGIN_PASSWORD", "login.password", DEFAULT_LOGIN_PASSWORD);

        String token = String.valueOf(System.currentTimeMillis())
                + ThreadLocalRandom.current().nextInt(100, 999);
        // Keep order-id short because panel applies a max-length rule.
        String runtimeOrderId = "AO" + token.substring(Math.max(0, token.length() - 6));
        String runtimeReferenceId = "REF" + token;
        String runtimePhone = "9" + token.substring(Math.max(0, token.length() - 9));
        String runtimeEmail = "qa+addorder_" + token + "@example.com";
        String runtimeDate = resolveDate(data, "");

        String buyerName = resolveDataValue(data.getBuyerName());
        String buyerPhone = resolveRuntimeValue(data.getBuyerPhone(), runtimePhone);
        String buyerAltPhone = resolveDataValue(data.getBuyerAlternatePhone());
        String buyerEmail = resolveRuntimeValue(data.getBuyerEmail(), runtimeEmail);
        String buyerGst = resolveDataValue(data.getBuyerGst());
        String line1 = resolveDataValue(data.getAddressLine1());
        String line2 = resolveDataValue(data.getAddressLine2());
        String pincode = resolveDataValue(data.getPincode());

        String warehouseTitle = resolveDataValue(data.getWarehouseTitle());
        String warehousePhone = resolveRuntimeValue(data.getWarehousePhone(), runtimePhone);
        String warehouseEmail = resolveRuntimeValue(data.getWarehouseEmail(), runtimeEmail);

        try {
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

            AddOrderPage addOrderPage = new AddOrderPage(driver);
            addOrderPage.openAddOrder(baseUrl);
            addOrderPage.waitUntilLoaded(Duration.ofSeconds(10));
            test.pass("Opened add order page",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_add_order_open"))
                            .build());

            addOrderPage.fillBuyerReceiverDetails(
                    buyerName, buyerPhone, buyerAltPhone, buyerEmail, buyerGst);
            test.pass("Filled buyer/receiver details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_buyer_details"))
                            .build());

            addOrderPage.fillAddressDetails(line1, line2, pincode);
            test.pass("Filled address details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_address_details"))
                            .build());

            addOrderPage.fillOrderDetails(
                    runtimeOrderId,
                    runtimeReferenceId,
                    resolveDataValue(data.getOrderType()),
                    runtimeDate);
            test.pass("Filled order details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_order_details"))
                            .build());

            addOrderPage.fillProductDetails(
                    resolveDataValue(data.getProductName()),
                    resolveDataValue(data.getQuantity()),
                    resolveDataValue(data.getUnitPrice()),
                    "",
                    "",
                    "",
                    "");
            test.pass("Filled product details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_product_details"))
                            .build());

            addOrderPage.selectPaymentDetails(
                    resolveDataValue(data.getPaymentType()),
                    resolveDataValue(data.getCodMode()),
                    resolveDataValue(data.getCodAmount()),
                    resolveDataValue(data.getShippingCharges()));
            test.pass("Selected payment details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_payment_details"))
                            .build());

            addOrderPage.selectWarehouseWithFallback(
                    warehouseTitle, warehousePhone, warehouseEmail, line1, pincode);
            test.pass("Selected warehouse details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_warehouse"))
                            .build());

            addOrderPage.fillWeightAndDimensions(
                    resolveDataValue(data.getTotalWeight()),
                    resolveDataValue(data.getLength()),
                    resolveDataValue(data.getWidth()),
                    resolveDataValue(data.getHeight()));
            test.pass("Filled weight and dimensions",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_weight_dimensions"))
                            .build());

            addOrderPage.fillOtherDetailsIfVisible(
                    resolveDataValue(data.getResellerName()),
                    resolveDataValue(data.getResellerGst()),
                    resolveDataValue(data.getEwayBill()));
            test.pass("Filled optional details",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_optional_details"))
                            .build());

            addOrderPage.clickSaveOrder();
            boolean saveConfirmed = addOrderPage.waitForOrderSaved(Duration.ofSeconds(8));
            if (saveConfirmed) {
                test.pass("Order saved successfully",
                        MediaEntityBuilder
                                .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_order_saved"))
                                .build());
            } else {
                test.info("No explicit save confirmation visible; proceeding with Save & Assign Courier flow.");
            }

            addOrderPage.openAssignCourier();
            addOrderPage.clickShipNow();
            Assert.assertTrue(
                    addOrderPage.waitForShipNowCompleted(Duration.ofSeconds(10)),
                    "Ship Now completion signal not found.");
            test.pass("Courier assigned successfully via Ship Now",
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_ship_now_success"))
                            .build());
        } catch (TimeoutException timeoutException) {
            test.fail("Timeout while executing add order flow: " + timeoutException.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_timeout"))
                            .build());
            Assert.fail("Timeout for dataset " + data.getDatasetId(), timeoutException);
        } catch (AssertionError | RuntimeException exception) {
            test.fail("Add order flow failed: " + exception.getMessage(),
                    MediaEntityBuilder
                            .createScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, data.getDatasetId() + "_failed"))
                            .build());
            throw exception;
        }
    }

    private String resolveRuntimeValue(String raw, String runtimeValue) {
        String value = resolveDataValue(raw);
        if (value.isBlank()) {
            return runtimeValue;
        }
        String lowered = value.toLowerCase(Locale.ROOT);
        if ("dyn_phone".equals(lowered) || "dyn_email".equals(lowered)
                || "runtime".equals(lowered) || "auto".equals(lowered)) {
            return runtimeValue;
        }
        return value;
    }

    private String resolveDate(AddOrderCaseData data, String fallback) {
        String raw = resolveDataValue(data.getOrderDate());
        return raw.isBlank() ? fallback : raw;
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
