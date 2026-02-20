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
 * Rate calculator page object with explicit waits and retry-safe element actions.
 */
public class RateCalculatorPage {
    private final WebDriver driver;
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final By[] PAGE_MARKERS = new By[] {
            By.id("originPincode"),
            By.id("deliveryAreaPincode"),
            By.id("approximateWeight"),
            By.xpath("//*[contains(normalize-space(),'Rate Calculator')]")
    };

    private static final By[] DOMESTIC_TAB = new By[] {
            By.xpath("//*[self::button or self::div or self::span][contains(normalize-space(),'Domestic')]"),
            By.xpath("//*[contains(@class,'tab') and contains(normalize-space(),'Domestic')]")
    };
    private static final By[] INTERNATIONAL_TAB = new By[] {
            By.xpath("//*[self::button or self::div or self::span][contains(normalize-space(),'International')]"),
            By.xpath("//*[contains(@class,'tab') and contains(normalize-space(),'International')]")
    };
    private static final By[] PACKAGE_TYPE_TRIGGER = new By[] {
            By.id("packageType"),
            By.name("packageType"),
            By.xpath("//*[contains(normalize-space(),'Package Type')]/following::*[@role='combobox'][1]"),
            By.xpath("//*[contains(normalize-space(),'Package Type')]/following::*[@aria-haspopup='listbox'][1]"),
            By.xpath("//*[contains(normalize-space(),'Package Type')]/following::*[self::button or self::div][1]")
    };

    private static final By[] ORIGIN_PINCODE_INPUT = new By[] {
            By.id("originPincode"),
            By.name("originPincode"),
            By.id("originPostalCode"),
            By.id("originZipCode"),
            By.xpath("//*[contains(normalize-space(),'Origin')]/following::input[1]")
    };
    private static final By[] DESTINATION_PINCODE_INPUT = new By[] {
            By.id("deliveryAreaPincode"),
            By.name("deliveryAreaPincode"),
            By.id("destinationPincode"),
            By.id("destinationPostalCode"),
            By.id("destinationZipCode"),
            By.id("destinationCountry"),
            By.xpath("//*[contains(normalize-space(),'Delivery Area Pincode')]/following::input[1]"),
            By.xpath("//*[contains(normalize-space(),'Destination')]/following::input[1]")
    };
    private static final By[] WEIGHT_INPUT = new By[] {
            By.id("approximateWeight"),
            By.name("approximateWeight"),
            By.xpath("//input[contains(@placeholder,'0.5')]"),
            By.xpath("//*[contains(normalize-space(),'Weight')]/following::input[1]")
    };
    private static final By[] INVOICE_VALUE_INPUT = new By[] {
            By.id("invoiceValue"),
            By.name("invoiceValue"),
            By.xpath("//*[contains(normalize-space(),'Invoice Value')]/following::input[1]")
    };
    private static final By[] LENGTH_INPUT = new By[] {
            By.id("lengthCM-0"),
            By.name("lengthCM-0")
    };
    private static final By[] WIDTH_INPUT = new By[] {
            By.id("widthCM-0"),
            By.name("widthCM-0")
    };
    private static final By[] HEIGHT_INPUT = new By[] {
            By.id("heightCM-0"),
            By.name("heightCM-0")
    };

    private static final By[] CALCULATE_BUTTON = new By[] {
            By.xpath("//button[contains(normalize-space(),'Calculate')]"),
            By.cssSelector("button[type='submit']")
    };

    private static final By[] RESULTS_MARKER = new By[] {
            By.xpath("//*[contains(normalize-space(),'Courier Partner')]"),
            By.xpath("//*[contains(normalize-space(),'Estimated Delivery')]"),
            By.xpath("//*[contains(normalize-space(),'Chargeable Weight')]"),
            By.xpath("//button[contains(normalize-space(),'Ship Now')]"),
            By.xpath("//*[contains(normalize-space(),'Charges')]"),
            By.xpath("//*[contains(normalize-space(),'Delivery')]")
    };

    private static final By[] VALIDATION_OR_STATUS = new By[] {
            By.cssSelector("[role='status'][aria-live='polite']"),
            By.cssSelector("[role='alert']"),
            By.cssSelector("p.MuiFormHelperText-root.Mui-error")
    };

    public RateCalculatorPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/tools/rate-calculator");
    }

    public void waitUntilLoaded(Duration timeout) {
        waitVisibleAny(PAGE_MARKERS, timeout);
    }

    public void selectCalculatorMode(String calculatorMode) {
        String mode = calculatorMode == null ? "" : calculatorMode.trim().toLowerCase();
        if (mode.isEmpty()) {
            return;
        }
        if (mode.contains("international")) {
            clickRequired(INTERNATIONAL_TAB, "International tab");
            return;
        }
        clickRequired(DOMESTIC_TAB, "Domestic tab");
    }

    public boolean trySelectPackageType(String packageType) {
        String packageValue = packageType == null ? "" : packageType.trim();
        if (packageValue.isBlank()) {
            return false;
        }

        WebElement trigger = findVisible(PACKAGE_TYPE_TRIGGER);
        if (trigger == null) {
            return false;
        }

        scrollToCenter(trigger);
        clickElement(trigger);

        String lowered = packageValue.toLowerCase();
        By optionLocator;
        if (lowered.contains("b2b")) {
            optionLocator = By.xpath("//*[(@role='option' or self::li or self::p or self::span)"
                    + " and contains(translate(normalize-space(),'" + UPPERCASE + "','" + LOWERCASE + "'),'b2b')]");
        } else if (lowered.contains("b2c")) {
            optionLocator = By.xpath("//*[(@role='option' or self::li or self::p or self::span)"
                    + " and contains(translate(normalize-space(),'" + UPPERCASE + "','" + LOWERCASE + "'),'b2c')]");
        } else {
            optionLocator = By.xpath("//*[(@role='option' or self::li or self::p or self::span)"
                    + " and contains(translate(normalize-space(),'" + UPPERCASE + "','" + LOWERCASE + "'),'"
                    + lowered + "')]");
        }

        WebElement option = waitVisibleAny(new By[] {optionLocator}, Duration.ofSeconds(5));
        if (option == null) {
            return false;
        }
        clickElement(option);
        return true;
    }

    public void fillForm(
            String originPincode,
            String destinationPincode,
            String weight,
            String invoiceValue,
            String length,
            String width,
            String height) {
        typeRequired(ORIGIN_PINCODE_INPUT, originPincode, "Origin pincode");
        typeRequired(DESTINATION_PINCODE_INPUT, destinationPincode, "Destination pincode");
        typeRequired(WEIGHT_INPUT, weight, "Weight");
        typeRequired(INVOICE_VALUE_INPUT, invoiceValue, "Invoice value");
        typeRequired(LENGTH_INPUT, length, "Length");
        typeRequired(WIDTH_INPUT, width, "Width");
        typeRequired(HEIGHT_INPUT, height, "Height");
    }

    public void clickCalculate() {
        clickRequired(CALCULATE_BUTTON, "Calculate");
    }

    public boolean waitForResults(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driverState -> isAnyVisible(RESULTS_MARKER));
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public String captureVisibleValidationText() {
        StringBuilder text = new StringBuilder();
        for (By locator : VALIDATION_OR_STATUS) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (!element.isDisplayed()) {
                        continue;
                    }
                    String value = element.getText();
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    String trimmed = value.trim();
                    if (text.toString().contains(trimmed)) {
                        continue;
                    }
                    if (text.length() > 0) {
                        text.append(" | ");
                    }
                    text.append(trimmed);
                } catch (StaleElementReferenceException ignored) {
                    // Status messages may animate in/out.
                }
            }
        }
        return text.toString();
    }

    public String capturePackageTypeValue() {
        WebElement trigger = findVisible(PACKAGE_TYPE_TRIGGER);
        if (trigger == null) {
            return "";
        }
        String value = trigger.getAttribute("value");
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        String text = trigger.getText();
        if (text != null && !text.isBlank()) {
            return text.trim();
        }
        return "";
    }

    private void typeRequired(By[] locators, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank.");
        }
        WebElement element = waitVisibleAny(locators, Duration.ofSeconds(10));
        if (element == null) {
            throw new IllegalStateException(fieldName + " input was not found.");
        }
        scrollToCenter(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        element.sendKeys(value.trim());
    }

    private void clickRequired(By[] locators, String actionName) {
        WebElement element = waitVisibleAny(locators, Duration.ofSeconds(10));
        if (element == null) {
            throw new IllegalStateException(actionName + " button was not found.");
        }
        scrollToCenter(element);
        clickElement(element);
    }

    private void clickElement(WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
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
                        // Keep scanning when DOM re-renders.
                    }
                }
            }
            return null;
        });
    }

    private boolean isAnyVisible(By[] locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        return true;
                    }
                } catch (StaleElementReferenceException ignored) {
                    // Ignore and continue searching.
                }
            }
        }
        return false;
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
                    // Ignore stale element and continue.
                }
            }
        }
        return null;
    }

    private void scrollToCenter(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
        } catch (RuntimeException ignored) {
            // Non-blocking, continue with direct interaction.
        }
    }
}
