package tests.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Add-order page object for Forward B2C flow until courier assignment.
 */
public class AddOrderPage {
    private final WebDriver driver;

    private static final By[] ADD_ORDER_NAV = new By[] {
            By.cssSelector("a[href='/orders/add']"),
            By.xpath("//a[contains(@href,'/orders/add')]"),
            By.xpath("//button[contains(normalize-space(),'Add Order')]"),
            By.xpath("//p[contains(normalize-space(),'Add Orders')]")
    };
    private static final By[] ADD_ORDER_PAGE_MARKER = new By[] {
            By.id("user-name"),
            By.id("addressLineOne"),
            By.xpath("//*[contains(normalize-space(),'Buyer') and contains(normalize-space(),'Receiver')]")
    };

    private static final By[] BUYER_NAME_INPUT = new By[] {
            By.id("user-name"),
            By.name("name"),
            By.xpath("//input[@placeholder='Enter name or search']"),
            By.xpath("//*[contains(normalize-space(),'Buyer Name')]/following::input[1]")
    };
    private static final By[] BUYER_PHONE_INPUT = new By[] {
            By.id("phone"),
            By.name("phone"),
            By.xpath("//input[@placeholder='Enter phone or search']"),
            By.xpath("//*[contains(normalize-space(),'Phone')]/following::input[1]")
    };
    private static final By[] BUYER_ALT_PHONE_INPUT = new By[] {
            By.id("alternativePhone"),
            By.id("altPhone"),
            By.xpath("//*[contains(normalize-space(),'Alternative phone')]/following::input[1]")
    };
    private static final By[] BUYER_EMAIL_INPUT = new By[] {
            By.id("email"),
            By.name("email"),
            By.xpath("//input[@placeholder='Email']"),
            By.xpath("//*[contains(normalize-space(),'Email ID')]/following::input[1]")
    };
    private static final By[] BUYER_GST_INPUT = new By[] {
            By.id("gst"),
            By.id("gstNumber"),
            By.name("gst"),
            By.xpath("//input[@placeholder='GSTIN']"),
            By.xpath("//*[contains(normalize-space(),'GSTIN')]/following::input[1]")
    };

    private static final By[] ADDRESS_LINE1_INPUT = new By[] {
            By.id("addressLineOne"),
            By.name("addressLineOne"),
            By.xpath("//input[@placeholder='Flat, House No. Building, Apartment']"),
            By.xpath("//*[contains(normalize-space(),'Address Line 1')]/following::input[1]")
    };
    private static final By[] ADDRESS_LINE2_INPUT = new By[] {
            By.id("addressLineTwo"),
            By.name("addressLineTwo"),
            By.xpath("//input[@placeholder='Area, Colony, Street No., Sector']"),
            By.xpath("//*[contains(normalize-space(),'Address Line 2')]/following::input[1]")
    };
    private static final By[] PINCODE_INPUT = new By[] {
            By.id("pincode"),
            By.name("pincode"),
            By.xpath("//input[@placeholder='Pincode']"),
            By.xpath("//*[contains(normalize-space(),'Pincode')]/following::input[1]")
    };
    private static final By[] CITY_INPUT = new By[] {
            By.id("city"),
            By.name("city"),
            By.xpath("//input[@placeholder='City']"),
            By.xpath("//*[contains(normalize-space(),'City')]/following::input[1]")
    };
    private static final By[] STATE_INPUT = new By[] {
            By.id("state"),
            By.name("state"),
            By.xpath("//input[@placeholder='State']"),
            By.xpath("//*[contains(normalize-space(),'State')]/following::input[1]")
    };
    private static final By[] COUNTRY_INPUT = new By[] {
            By.id("country"),
            By.name("country"),
            By.xpath("//input[@placeholder='Country']"),
            By.xpath("//*[contains(normalize-space(),'Country')]/following::input[1]")
    };

    private static final By[] ORDER_ID_INPUT = new By[] {
            By.id("orderId"),
            By.name("orderId"),
            By.xpath("//input[contains(@placeholder,'Order ID') or contains(@placeholder,'Order Id')]"),
            By.xpath("//*[contains(normalize-space(),'Order ID')]/following::input[1]")
    };
    private static final By[] ORDER_DATE_INPUT = new By[] {
            By.id("orderDate"),
            By.name("orderDate"),
            By.xpath("//*[contains(normalize-space(),'Order Date')]/following::input[1]")
    };
    private static final By[] REFERENCE_ID_INPUT = new By[] {
            By.id("referenceId"),
            By.name("referenceId"),
            By.xpath("//input[contains(@placeholder,'Reference')]"),
            By.xpath("//*[contains(normalize-space(),'Reference')]/following::input[1]")
    };
    private static final By ESSENTIAL_ORDER_TYPE = By.xpath(
            "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'essential')]");
    private static final By NON_ESSENTIAL_ORDER_TYPE = By.xpath(
            "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'non essential') "
                    + "or contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'non-essential')]");

    private static final By[] PRODUCT_NAME_INPUT = new By[] {
            By.id("product-name"),
            By.name("product-name"),
            By.xpath("//input[contains(@placeholder,'Product Name') or contains(@placeholder,'product name')]"),
            By.xpath("//*[contains(normalize-space(),'Product Name')]/following::input[1]")
    };
    private static final By[] QUANTITY_INPUT = new By[] {
            By.id("qty-0"),
            By.name("qty-0"),
            By.xpath("//input[contains(@placeholder,'Qty') or contains(@placeholder,'Quantity')]"),
            By.xpath("//*[contains(normalize-space(),'Quantity')]/following::input[1]")
    };
    private static final By[] UNIT_PRICE_INPUT = new By[] {
            By.id("unitPrice-0"),
            By.name("unitPrice-0"),
            By.xpath("//input[contains(@placeholder,'Unit Price') or contains(@placeholder,'Price')]"),
            By.xpath("//*[contains(normalize-space(),'Unit Price')]/following::input[1]")
    };
    private static final By[] HSN_INPUT = new By[] {By.id("hsn-0"), By.name("hsn-0")};
    private static final By[] SKU_INPUT = new By[] {By.id("sku-0"), By.name("sku-0"), By.id("sku")};
    private static final By[] DISCOUNT_INPUT = new By[] {By.id("discount-0"), By.name("discount-0")};
    private static final By[] TAX_INPUT = new By[] {By.id("tax-0"), By.name("tax-0")};
    private static final By[] GRAND_TOTAL_VALUE = new By[] {
            By.id("grandTotal"),
            By.xpath("//*[contains(normalize-space(),'Grand Total')]/following::*[1]")
    };

    private static final By PREPAID_PAYMENT_TYPE = By.xpath(
            "//input[@name='paymentType' and (@value='0' or @value='PREPAID')] | "
                    + "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'prepaid')]");
    private static final By COD_PAYMENT_TYPE = By.xpath(
            "//input[@name='paymentType' and (@value='1' or @value='COD')] | "
                    + "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cod')]");
    private static final By COD_MODE_INVOICE = By.xpath(
            "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invoice')]");
    private static final By COD_MODE_MANUAL = By.xpath(
            "//label[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'manual')]");
    private static final By[] COD_AMOUNT_INPUT = new By[] {By.id("codAmount"), By.name("codAmount")};
    private static final By[] SHIPPING_CHARGES_INPUT = new By[] {By.id("shippingCharge"), By.name("shippingCharge")};

    private static final By[] SAVED_WAREHOUSE_DEFAULT = new By[] {
            By.xpath("//p[span[contains(normalize-space(),'(Default)')]]"),
            By.xpath("//*[contains(normalize-space(),'(Default)')]")
    };
    private static final By[] ADD_ADDRESS_BUTTON = new By[] {
            By.xpath("//button[contains(normalize-space(),'Add Address')]"),
            By.xpath("//*[contains(normalize-space(),'Add Address')]")
    };
    private static final By[] WAREHOUSE_TITLE_INPUT = new By[] {By.id("title"), By.name("title"), By.id("addressTitle")};
    private static final By[] WAREHOUSE_PHONE_INPUT = new By[] {By.id("warehousePhone"), By.id("phone"), By.name("phone")};
    private static final By[] WAREHOUSE_EMAIL_INPUT = new By[] {By.id("warehouseEmail"), By.id("email"), By.name("email")};
    private static final By[] WAREHOUSE_LINE1_INPUT = new By[] {
            By.id("warehouseAddressLineOne"),
            By.id("addressLineOne"),
            By.name("addressLineOne")
    };
    private static final By[] WAREHOUSE_PINCODE_INPUT = new By[] {
            By.id("warehousePincode"),
            By.id("pincode"),
            By.name("pincode")
    };
    private static final By[] SAVE_ADDRESS_BUTTON = new By[] {
            By.xpath("//div[@role='dialog']//button[contains(normalize-space(),'Save')]"),
            By.xpath("//button[contains(normalize-space(),'Save Address')]")
    };
    private static final By OTP_INPUT_0 = By.id("otpInput-0");

    private static final By[] TOTAL_WEIGHT_INPUT = new By[] {
            By.id("totalWeight"),
            By.name("totalWeight"),
            By.xpath("//input[contains(@placeholder,'Total Weight') or contains(@placeholder,'Weight')]"),
            By.xpath("//*[contains(normalize-space(),'Total Weight')]/following::input[1]")
    };
    private static final By[] LENGTH_INPUT = new By[] {
            By.id("lengthCM-0"),
            By.name("lengthCM-0"),
            By.xpath("//input[contains(@placeholder,'Length')]"),
            By.xpath("//*[contains(normalize-space(),'Length')]/following::input[1]")
    };
    private static final By[] WIDTH_INPUT = new By[] {
            By.id("widthCM-0"),
            By.name("widthCM-0"),
            By.xpath("//input[contains(@placeholder,'Width')]"),
            By.xpath("//*[contains(normalize-space(),'Width')]/following::input[1]")
    };
    private static final By[] HEIGHT_INPUT = new By[] {
            By.id("heightCM-0"),
            By.name("heightCM-0"),
            By.xpath("//input[contains(@placeholder,'Height')]"),
            By.xpath("//*[contains(normalize-space(),'Height')]/following::input[1]")
    };
    private static final By SINGLE_PACKAGE_B2C = By.xpath(
            "//*[self::label or self::span][contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'single') "
                    + "and contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'b2c')]");

    private static final By[] RESELLER_NAME_INPUT = new By[] {By.id("resellerName"), By.name("resellerName")};
    private static final By[] RESELLER_GST_INPUT = new By[] {By.id("resellerGst"), By.name("resellerGst")};
    private static final By[] EWAY_BILL_INPUT = new By[] {By.id("ewayBill"), By.name("ewayBill")};

    private static final By[] SAVE_ORDER_BUTTON = new By[] {
            By.xpath("//button[contains(normalize-space(),'Save') and @type='button']"),
            By.xpath("//button[contains(normalize-space(),'Save')]")
    };
    private static final By[] SAVE_AND_ASSIGN_BUTTON = new By[] {
            By.xpath("//button[contains(normalize-space(),'Save & Assign Courier')]"),
            By.xpath("//button[contains(normalize-space(),'Save and Assign Courier')]"),
            By.xpath("//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'save') "
                    + "and contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'assign')]")
    };
    private static final By[] ORDER_SUCCESS_TOAST = new By[] {
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'order') "
                    + "and contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'created')]"),
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saved successfully')]")
    };
    private static final By[] ASSIGN_COURIER_BUTTON = new By[] {
            By.xpath("//button[normalize-space()='Assign Courier']"),
            By.xpath("//button[contains(normalize-space(),'Assign Courier')]")
    };
    private static final By[] SHIP_NOW_BUTTON = new By[] {
            By.xpath("//button[normalize-space()='Ship Now']"),
            By.xpath("//button[contains(normalize-space(),'Ship Now')]")
    };
    private static final By[] SHIP_NOW_SUCCESS = new By[] {
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'assigned')]"),
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pickup')]"),
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")
    };
    private static final By[] IMMEDIATE_TOAST = new By[] {
            By.cssSelector("[role='status'][aria-live='polite']"),
            By.cssSelector(".MuiAlert-message"),
            By.cssSelector("[role='alert']"),
            By.xpath("//*[contains(@class,'toast') and string-length(normalize-space()) > 0]")
    };
    private static final By[] FORM_OR_TOAST_ERRORS = new By[] {
            By.cssSelector("[role='status'][aria-live='polite']"),
            By.cssSelector("p.MuiFormHelperText-root.Mui-error"),
            By.cssSelector(".MuiAlert-message"),
            By.xpath("//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cannot')]")
    };

    public AddOrderPage(WebDriver driver) {
        this.driver = driver;
    }

    public void openAddOrder(String baseUrl) {
        if (driver.getCurrentUrl().contains("/orders/add")) {
            return;
        }

        if (clickIfPresent(ADD_ORDER_NAV)) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.urlContains("/orders/add"));
                return;
            } catch (TimeoutException ignored) {
                // Fallback to direct URL when side navigation click does not redirect.
            }
        }

        driver.get(baseUrl + "/orders/add");
    }

    public void waitUntilLoaded(Duration timeout) {
        waitVisibleAny(ADD_ORDER_PAGE_MARKER, timeout);
    }

    public void fillBuyerReceiverDetails(
            String name,
            String phone,
            String alternatePhone,
            String email,
            String gst) {
        typeRequired(BUYER_NAME_INPUT, name, "Buyer name");
        typeRequired(BUYER_PHONE_INPUT, phone, "Buyer phone");
        typeOptional(BUYER_ALT_PHONE_INPUT, alternatePhone);
        typeOptional(BUYER_EMAIL_INPUT, email);
        typeOptional(BUYER_GST_INPUT, gst);
    }

    public void fillAddressDetails(String line1, String line2, String pincode) {
        typeRequired(ADDRESS_LINE1_INPUT, line1, "Address line 1");
        typeOptional(ADDRESS_LINE2_INPUT, line2);
        typeRequired(PINCODE_INPUT, pincode, "Pincode");

        if (!isCityStateCountryAutofilled(Duration.ofSeconds(8))) {
            throw new IllegalStateException("Pincode city/state/country autofill did not populate.");
        }
    }

    public void fillOrderDetails(
            String runtimeOrderId,
            String runtimeReferenceId,
            String orderType,
            String orderDate) {
        openSection("Order Details");
        typeOptional(ORDER_ID_INPUT, runtimeOrderId);
        typeOptional(REFERENCE_ID_INPUT, runtimeReferenceId);
        typeOptional(ORDER_DATE_INPUT, orderDate);

        if (orderType != null && !orderType.isBlank()
                && orderType.toLowerCase().contains("non")) {
            clickIfPresent(new By[] {NON_ESSENTIAL_ORDER_TYPE});
        } else {
            clickIfPresent(new By[] {ESSENTIAL_ORDER_TYPE});
        }
    }

    public void fillProductDetails(
            String productName,
            String quantity,
            String unitPrice,
            String hsn,
            String sku,
            String discount,
            String tax) {
        typeRequired(PRODUCT_NAME_INPUT, productName, "Product name");
        typeRequired(QUANTITY_INPUT, quantity, "Product quantity");
        typeRequired(UNIT_PRICE_INPUT, unitPrice, "Unit price");
        typeOptional(HSN_INPUT, hsn);
        typeOptional(SKU_INPUT, sku);
        typeOptional(DISCOUNT_INPUT, discount);
        typeOptional(TAX_INPUT, tax);

        // Basic guard to ensure totals block is visible after product inputs.
        waitVisibleAny(GRAND_TOTAL_VALUE, Duration.ofSeconds(5));
    }

    public void selectPaymentDetails(String paymentType, String codMode, String codAmount, String shippingCharges) {
        if (paymentType != null && paymentType.toLowerCase().contains("cod")) {
            clickIfPresent(new By[] {COD_PAYMENT_TYPE});
            if (codMode != null && codMode.toLowerCase().contains("manual")) {
                clickIfPresent(new By[] {COD_MODE_MANUAL});
            } else {
                clickIfPresent(new By[] {COD_MODE_INVOICE});
            }
            typeOptional(COD_AMOUNT_INPUT, codAmount);
        } else {
            clickIfPresent(new By[] {PREPAID_PAYMENT_TYPE});
        }
        typeOptional(SHIPPING_CHARGES_INPUT, shippingCharges);
    }

    public void selectWarehouseWithFallback(
            String warehouseTitle,
            String warehousePhone,
            String warehouseEmail,
            String warehouseLine1,
            String warehousePincode) {
        openSection("Warehouse");
        if (clickIfPresent(SAVED_WAREHOUSE_DEFAULT)) {
            return;
        }

        if (!clickIfPresent(ADD_ADDRESS_BUTTON)) {
            throw new IllegalStateException(
                    "No saved warehouse found and Add Address is unavailable. Precondition: create a default warehouse.");
        }

        typeOptional(WAREHOUSE_TITLE_INPUT, warehouseTitle);
        typeOptional(WAREHOUSE_PHONE_INPUT, warehousePhone);
        typeOptional(WAREHOUSE_EMAIL_INPUT, warehouseEmail);
        typeOptional(WAREHOUSE_LINE1_INPUT, warehouseLine1);
        typeOptional(WAREHOUSE_PINCODE_INPUT, warehousePincode);

        if (isOtpStepVisible()) {
            throw new IllegalStateException(
                    "Warehouse creation requires OTP verification. Precondition: keep at least one saved warehouse address.");
        }

        if (!clickIfPresent(SAVE_ADDRESS_BUTTON)) {
            throw new IllegalStateException("Add Address dialog is open, but Save button is not available.");
        }

        new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(driverState -> !driverState.findElements(OTP_INPUT_0).isEmpty() || clickIfPresent(SAVED_WAREHOUSE_DEFAULT));

        if (isOtpStepVisible()) {
            throw new IllegalStateException(
                    "Warehouse creation reached OTP step. Precondition: use saved/default warehouse for automation.");
        }
        if (!clickIfPresent(SAVED_WAREHOUSE_DEFAULT)) {
            throw new IllegalStateException(
                    "Warehouse address could not be selected after Add Address fallback.");
        }
    }

    public void fillWeightAndDimensions(String weight, String length, String width, String height) {
        openSection("Weight");
        clickIfPresent(new By[] {SINGLE_PACKAGE_B2C});
        typeRequired(TOTAL_WEIGHT_INPUT, weight, "Total weight");
        typeRequired(LENGTH_INPUT, length, "Length");
        typeRequired(WIDTH_INPUT, width, "Width");
        typeRequired(HEIGHT_INPUT, height, "Height");
    }

    public void fillOtherDetailsIfVisible(String resellerName, String resellerGst, String ewayBill) {
        openSection("Other");
        typeOptional(RESELLER_NAME_INPUT, resellerName);
        typeOptional(RESELLER_GST_INPUT, resellerGst);
        typeOptional(EWAY_BILL_INPUT, ewayBill);
    }

    public void clickSaveOrder() {
        clickRequired(SAVE_ORDER_BUTTON, "Save Order");
    }

    public boolean waitForOrderSaved(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driverState -> {
                String url = driverState.getCurrentUrl().toLowerCase();
                if (url.contains("/orders/new") || url.contains("/orders/assign-courier")) {
                    return true;
                }
                return isAnyVisible(ORDER_SUCCESS_TOAST);
            });
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public void openAssignCourier() {
        if (driver.getCurrentUrl().contains("/orders/assign-courier")) {
            return;
        }
        String immediateToastText = "";
        String submitPath = "unknown";
        boolean orderCreationSignal = false;
        if (driver.getCurrentUrl().contains("/orders/add")) {
            submitPath = "orders/add_save_assign";
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            boolean clickedSaveAssign = clickIfPresent(SAVE_AND_ASSIGN_BUTTON);

            if (clickedSaveAssign) {
                immediateToastText = waitAndReadToast(Duration.ofSeconds(6));
                orderCreationSignal = isOrderCreationSignal(immediateToastText) || isAnyVisible(ORDER_SUCCESS_TOAST);
                if (waitForAssignStepWithLoaderRecovery(Duration.ofSeconds(10))) {
                    return;
                }
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
                clickedSaveAssign = clickIfPresent(SAVE_AND_ASSIGN_BUTTON);
                if (clickedSaveAssign) {
                    String secondToast = waitAndReadToast(Duration.ofSeconds(6));
                    if (!secondToast.isBlank()) {
                        immediateToastText = secondToast;
                    }
                    orderCreationSignal = orderCreationSignal
                            || isOrderCreationSignal(secondToast)
                            || isAnyVisible(ORDER_SUCCESS_TOAST);
                    if (waitForAssignStepWithLoaderRecovery(Duration.ofSeconds(10))) {
                        return;
                    }
                }
            } else {
                submitPath = "orders/add_save_then_assign";
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                if (!clickIfPresent(SAVE_ORDER_BUTTON)) {
                    if (tryOpenAssignFromOrdersList()) {
                        return;
                    }
                    throw new TimeoutException("Save button not visible on /orders/add and assign list fallback failed.");
                }
                waitForOrderSaved(Duration.ofSeconds(10));
                if (driver.getCurrentUrl().contains("/orders/new")) {
                    orderCreationSignal = true;
                    clickRequired(ASSIGN_COURIER_BUTTON, "Assign Courier");
                    if (waitForAssignStepWithLoaderRecovery(Duration.ofSeconds(10))) {
                        return;
                    }
                }
            }
        } else {
            submitPath = "orders/new_assign_button";
            orderCreationSignal = driver.getCurrentUrl().contains("/orders/new");
            clickRequired(ASSIGN_COURIER_BUTTON, "Assign Courier");
        }
        try {
            if (waitForAssignStepWithLoaderRecovery(Duration.ofSeconds(10))) {
                return;
            }
            throw new TimeoutException("Assign step not visible yet.");
        } catch (TimeoutException timeoutException) {
            if (orderCreationSignal && tryOpenAssignFromOrdersList()) {
                return;
            }
            String details = collectVisibleMessages(FORM_OR_TOAST_ERRORS);
            details = details.isBlank()
                    ? "No visible validation text captured."
                    : details;
            String invalidFieldSummary = "";
            if (!immediateToastText.isBlank()) {
                details = details + " Immediate toast: " + immediateToastText;
            }
            try {
                Object result = ((JavascriptExecutor) driver).executeScript(
                        "return Array.from(document.querySelectorAll('input[aria-invalid=\"true\"],textarea[aria-invalid=\"true\"]'))"
                                + ".map(e => (e.getAttribute('placeholder') || e.getAttribute('name') || e.id || 'unknown').trim())"
                                + ".filter(v => v.length > 0);");
                if (result instanceof List && !((List<?>) result).isEmpty()) {
                    invalidFieldSummary = " Invalid fields: " + String.join(", ", ((List<?>) result).stream()
                            .map(String::valueOf)
                            .toArray(String[]::new));
                }
            } catch (RuntimeException ignored) {
                // Ignore JS extraction errors and return fallback timeout message.
            }
            String runtimeState = collectRuntimeState();
            throw new TimeoutException(
                    "Save & Assign Courier did not redirect to assign page. Validation details: "
                            + details + "." + invalidFieldSummary
                            + " Submit path: " + submitPath + ". Runtime state: " + runtimeState + ".",
                    timeoutException);
        }
    }

    private boolean isShipmozoLoaderScreen() {
        try {
            List<WebElement> logos = driver.findElements(By.xpath("//*[normalize-space()='SHIPMOZO']"));
            boolean hasLoaderText = false;
            for (WebElement logo : logos) {
                if (logo.isDisplayed()) {
                    hasLoaderText = true;
                    break;
                }
            }
            if (!hasLoaderText) {
                return false;
            }
            boolean hasAppShell = !driver.findElements(By.xpath(
                    "//input[contains(@placeholder,'Search Order by AWB ID')]"
                            + " | //button[contains(normalize-space(),'Quick Actions')]"
                            + " | //button[contains(normalize-space(),'Tickets')]")).isEmpty();
            return !hasAppShell;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean tryOpenAssignFromOrdersList() {
        String currentUrl = driver.getCurrentUrl();
        int slashSlash = currentUrl.indexOf("//");
        if (slashSlash < 0) {
            return false;
        }
        int firstSlashAfterHost = currentUrl.indexOf('/', slashSlash + 2);
        String origin = firstSlashAfterHost > 0 ? currentUrl.substring(0, firstSlashAfterHost) : currentUrl;
        driver.get(origin + "/orders/new");

        if (!clickIfPresent(ASSIGN_COURIER_BUTTON)) {
            return false;
        }
        return waitForAssignStep(Duration.ofSeconds(10));
    }

    private boolean waitForAssignStepWithLoaderRecovery(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        int refreshCount = 0;
        long nextRefreshAt = System.currentTimeMillis() + 12000;
        boolean assignFromOrdersNewClicked = false;
        while (System.currentTimeMillis() < deadline) {
            if (waitForAssignStep(Duration.ofSeconds(2))) {
                return true;
            }
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (currentUrl.contains("/orders/new")
                    && !isShipmozoLoaderScreen()
                    && !assignFromOrdersNewClicked) {
                assignFromOrdersNewClicked = clickIfPresent(ASSIGN_COURIER_BUTTON);
            }
            if (isShipmozoLoaderScreen()
                    && refreshCount < 3
                    && System.currentTimeMillis() >= nextRefreshAt) {
                driver.navigate().refresh();
                refreshCount++;
                nextRefreshAt = System.currentTimeMillis() + 12000;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private boolean waitForAssignStep(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driverState -> {
                String url = driverState.getCurrentUrl().toLowerCase();
                if (url.contains("/orders/assign-courier")) {
                    return true;
                }
                return isAnyVisible(SHIP_NOW_BUTTON);
            });
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private String waitAndReadToast(Duration timeout) {
        long end = System.currentTimeMillis() + timeout.toMillis();
        String fallbackText = "";
        while (System.currentTimeMillis() < end) {
            for (By locator : IMMEDIATE_TOAST) {
                List<WebElement> toasts = driver.findElements(locator);
                for (WebElement toast : toasts) {
                    try {
                        if (!toast.isDisplayed()) {
                            continue;
                        }
                        String text = toast.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            String normalized = text.trim();
                            if (isMeaningfulPopupText(normalized)) {
                                return normalized;
                            }
                            if (fallbackText.isBlank()) {
                                fallbackText = normalized;
                            }
                        }
                    } catch (StaleElementReferenceException ignored) {
                        // Toast may animate in/out quickly.
                    }
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return "";
            }
        }
        return fallbackText;
    }

    private String collectVisibleMessages(By[] locators) {
        StringBuilder collected = new StringBuilder();
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    String text = element.getText();
                    if (text == null || text.isBlank()) {
                        continue;
                    }
                    String trimmed = text.trim();
                    if (collected.toString().contains(trimmed)) {
                        continue;
                    }
                    if (collected.length() > 0) {
                        collected.append(" | ");
                    }
                    collected.append(trimmed);
                } catch (StaleElementReferenceException ignored) {
                    // Skip stale animated messages.
                }
            }
        }
        return collected.toString();
    }

    private String collectRuntimeState() {
        String currentUrl = driver.getCurrentUrl();
        String readyState = "unknown";
        String loaderState = String.valueOf(isShipmozoLoaderScreen());
        String title = "";
        try {
            Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState;");
            if (state != null) {
                readyState = String.valueOf(state);
            }
        } catch (RuntimeException ignored) {
            // Ignore script errors in diagnostics mode.
        }
        try {
            title = driver.getTitle();
        } catch (RuntimeException ignored) {
            // Ignore title read errors in diagnostics mode.
        }
        return "url=" + currentUrl + ", readyState=" + readyState
                + ", loaderScreen=" + loaderState + ", title=" + title;
    }

    private boolean isOrderCreationSignal(String toastText) {
        if (toastText == null || toastText.isBlank()) {
            return false;
        }
        String normalized = toastText.trim().toLowerCase();
        return normalized.contains("order created")
                || normalized.contains("saved successfully");
    }

    private boolean isMeaningfulPopupText(String text) {
        String normalized = text.toLowerCase();
        if (normalized.contains("volumetric weight")) {
            return false;
        }
        return normalized.contains("order")
                || normalized.contains("required")
                || normalized.contains("invalid")
                || normalized.contains("cannot")
                || normalized.contains("greater than")
                || normalized.contains("success")
                || normalized.contains("failed")
                || normalized.contains("error");
    }

    public void clickShipNow() {
        clickRequired(SHIP_NOW_BUTTON, "Ship Now");
    }

    public boolean waitForShipNowCompleted(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driverState -> {
                String url = driverState.getCurrentUrl().toLowerCase();
                if (url.contains("/orders/pickup")) {
                    return true;
                }
                return isAnyVisible(SHIP_NOW_SUCCESS);
            });
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private void openSection(String sectionText) {
        By locator = By.xpath("//*[self::p or self::span or self::button]"
                + "[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + sectionText.toLowerCase() + "')]");
        List<WebElement> elements = driver.findElements(locator);
        if (elements.isEmpty()) {
            return;
        }
        clickElement(elements.get(0));
    }

    private boolean isOtpStepVisible() {
        List<WebElement> otpFields = driver.findElements(OTP_INPUT_0);
        if (otpFields.isEmpty()) {
            return false;
        }
        try {
            return otpFields.get(0).isDisplayed();
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }

    private boolean isCityStateCountryAutofilled(Duration timeout) {
        List<WebElement> fields = findAllVisible(CITY_INPUT);
        fields.addAll(findAllVisible(STATE_INPUT));
        fields.addAll(findAllVisible(COUNTRY_INPUT));

        if (fields.isEmpty()) {
            return true;
        }

        try {
            new WebDriverWait(driver, timeout).until(driverState -> {
                for (WebElement field : fields) {
                    try {
                        String value = field.getAttribute("value");
                        if (value != null && !value.trim().isEmpty()) {
                            return true;
                        }
                    } catch (StaleElementReferenceException ignored) {
                        // Keep polling while the form re-renders.
                    }
                }
                return false;
            });
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private void typeRequired(By[] locators, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        WebElement element = waitVisibleAny(locators, Duration.ofSeconds(10));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        element.sendKeys(value.trim());
    }

    private void typeOptional(By[] locators, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        WebElement element = findVisible(locators);
        if (element == null) {
            return;
        }
        try {
            element.clear();
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"), value.trim());
        } catch (RuntimeException ignored) {
            element.sendKeys(value.trim());
        }
    }

    private void clickRequired(By[] locators, String actionName) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement element = waitVisibleAny(locators, Duration.ofSeconds(10));
            if (element == null) {
                throw new IllegalStateException(actionName + " element was not found.");
            }
            try {
                clickElement(element);
                return;
            } catch (RuntimeException runtimeException) {
                lastException = runtimeException;
                pauseShort(200);
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }

    private boolean clickIfPresent(By[] locators) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            WebElement element = findVisible(locators);
            if (element == null) {
                return false;
            }
            try {
                clickElement(element);
                return true;
            } catch (RuntimeException ignored) {
                pauseShort(200);
            }
        }
        return false;
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (RuntimeException firstException) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (RuntimeException secondException) {
                throw secondException;
            }
        }
    }

    private void pauseShort(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    private WebElement waitVisibleAny(By[] locators, Duration timeout) {
        return new WebDriverWait(driver, timeout).until(driverState -> {
            for (By locator : locators) {
                List<WebElement> elements = driverState.findElements(locator);
                for (WebElement element : elements) {
                    try {
                        if (element.isDisplayed()) {
                            return element;
                        }
                    } catch (StaleElementReferenceException ignored) {
                        // Ignore stale node and continue scanning available nodes.
                    }
                }
            }
            return null;
        });
    }

    private WebElement findVisible(By[] locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        return element;
                    }
                } catch (StaleElementReferenceException ignored) {
                    // Ignore stale node and keep searching.
                }
            }
        }
        return null;
    }

    private List<WebElement> findAllVisible(By[] locators) {
        java.util.ArrayList<WebElement> visible = new java.util.ArrayList<>();
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        visible.add(element);
                    }
                } catch (StaleElementReferenceException ignored) {
                    // Ignore stale node and keep scanning.
                }
            }
        }
        return visible;
    }

    private boolean isAnyVisible(By[] locators) {
        return findVisible(locators) != null;
    }
}
