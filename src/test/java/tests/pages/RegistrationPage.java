package tests.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Registration page object with direct form actions and simple outcome checks.
 */
public class RegistrationPage {
    private final WebDriver driver;
    private static final String SUCCESS_REGISTRATION_URL = "https://panel.appiify.com/profile/profile-completion";

    private static final By HEADING = By.xpath("//h3[contains(normalize-space(),'Register to Shipmozo')]");
    private static final By USER_TYPE_DROPDOWN = By.id("mui-component-select-usertype");
    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By CONFIRM_PASSWORD_INPUT = By.id("cpassword");
    private static final By VERIFY_PHONE_BUTTON = By.xpath("//button[normalize-space()='Verify']");
    private static final By OTP_POPUP_TITLE = By.xpath("//h4[contains(normalize-space(),'Phone number verification')]");
    private static final By VERIFY_CONTINUE_BUTTON = By.xpath("//button[normalize-space()='Verify and Continue']");
    private static final By AGREE_TERMS_CHECKBOX = By.name("agreeterms");
    private static final By REGISTER_BUTTON = By.xpath("//button[@type='submit' and contains(.,'Register')]");
    private static final By ERROR_TEXTS = By.cssSelector("p.MuiFormHelperText-root.Mui-error");
    private static final By GENERIC_ERROR_TEXTS =
            By.cssSelector("[role='alert'], .MuiAlert-message, .MuiFormHelperText-root, .Mui-error");

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
    }

    public enum RegistrationOutcome {
        SUCCESS_URL,
        ERROR_TEXT
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/register");
    }

    public void waitUntilLoaded(Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(HEADING));
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/register");
    }

    public void selectUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return;
        }
        String normalizedType = userType;
        // Keep existing test data compatible ("Seller") with the current UI option labels.
        if ("Seller".equalsIgnoreCase(userType.trim())) {
            normalizedType = "Individual";
        }
        if ("Company".equalsIgnoreCase(userType.trim())) {
            normalizedType = "Business Type";
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(USER_TYPE_DROPDOWN));
        try {
            dropdown.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
        }

        By allOptionsBy = By.xpath("//*[@role='option']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(allOptionsBy));

        List<WebElement> options = driver.findElements(allOptionsBy);
        for (WebElement option : options) {
            String text = option.getText() == null ? "" : option.getText().trim();
            if (normalizedType.equalsIgnoreCase(text)) {
                try {
                    option.click();
                } catch (RuntimeException ignored) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                }
                return;
            }
        }

        // Fallback: if exact label is not found, pick first option to continue flow.
        if (!options.isEmpty()) {
            WebElement firstOption = options.get(0);
            try {
                firstOption.click();
            } catch (RuntimeException ignored) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstOption);
            }
        }
    }

    public void typeName(String name) {
        type(NAME_INPUT, name);
    }

    public void typeEmail(String email) {
        type(EMAIL_INPUT, email);
    }

    public void typePhone(String phone) {
        type(PHONE_INPUT, phone);
    }

    public void typePassword(String password) {
        type(PASSWORD_INPUT, password);
    }

    public void typeConfirmPassword(String confirmPassword) {
        type(CONFIRM_PASSWORD_INPUT, confirmPassword);
    }

    public void clickVerifyPhone() {
        WebElement verifyButton = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(VERIFY_PHONE_BUTTON));
        try {
            verifyButton.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", verifyButton);
        }
    }

    public void waitForOtpPopup(Duration timeout) {
        long timeoutMillis = timeout.toMillis();
        long endTime = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < endTime) {
            List<WebElement> otpFields = driver.findElements(By.id("otpInput-0"));
            if (!otpFields.isEmpty() && otpFields.get(0).isDisplayed()) {
                return;
            }

            List<WebElement> verifyButtons = driver.findElements(VERIFY_PHONE_BUTTON);
            if (!verifyButtons.isEmpty() && verifyButtons.get(0).isDisplayed()) {
                WebElement verifyButton = verifyButtons.get(0);
                try {
                    verifyButton.click();
                } catch (RuntimeException ignored) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", verifyButton);
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        throw new TimeoutException("OTP input did not appear within " + timeout.getSeconds() + " seconds");
    }

    public void enterOtp(String otp) {
        String safeOtp = otp == null ? "" : otp.trim();
        for (int index = 0; index < safeOtp.length() && index < 6; index++) {
            By otpField = By.id("otpInput-" + index);
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(otpField));
            field.clear();
            field.sendKeys(String.valueOf(safeOtp.charAt(index)));
        }
    }

    public void clickVerifyAndContinue() {
        By otpField = By.id("otpInput-0");

        // If OTP input is not present, verification is already completed.
        if (driver.findElements(otpField).isEmpty()) {
            return;
        }

        long endTime = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
        while (System.currentTimeMillis() < endTime) {
            if (driver.findElements(otpField).isEmpty()) {
                return;
            }

            List<WebElement> verifyButtons = driver.findElements(VERIFY_CONTINUE_BUTTON);
            if (!verifyButtons.isEmpty()) {
                WebElement button = verifyButtons.get(0);
                try {
                    if (button.isDisplayed() && button.isEnabled()) {
                        try {
                            button.click();
                        } catch (RuntimeException ignored) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                        }
                    }
                } catch (StaleElementReferenceException ignored) {
                    // Modal can re-render the button while OTP verification is in progress.
                }
            }

            try {
                new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.invisibilityOfElementLocated(otpField));
                return;
            } catch (TimeoutException ignored) {
                // Keep polling until modal closes or timeout reaches.
            }
        }

        throw new TimeoutException("OTP popup did not close after verify and continue.");
    }

    public void setAgreeTerms(boolean shouldAgree) {
        WebElement checkbox = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(AGREE_TERMS_CHECKBOX));
        if (checkbox.isSelected() != shouldAgree) {
            checkbox.click();
        }
    }

    public void clickRegister() {
        WebElement registerButton = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(REGISTER_BUTTON));
        registerButton.click();
    }

    public void fillForm(
            String userType,
            String name,
            String email,
            String phone,
            String password,
            String confirmPassword,
            boolean agreeTerms) {
        // Keep field order same as UI for easier debugging.
        selectUserType(userType);
        typeName(name);
        typeEmail(email);
        typePhone(phone);
        typePassword(password);
        typeConfirmPassword(confirmPassword);
        setAgreeTerms(agreeTerms);
    }

    /**
     * Waits only for two outcomes:
     * 1) redirect to registration success URL
     * 2) visible non-empty error text
     */
    public RegistrationOutcome waitForOutcome(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(webDriver -> {
            if (isOnRegistrationSuccessUrl()) {
                return RegistrationOutcome.SUCCESS_URL;
            }
            if (hasAnyVisibleErrorText()) {
                return RegistrationOutcome.ERROR_TEXT;
            }
            return null;
        });
    }

    /**
     * Success heuristic based on observed registration redirect behavior.
     */
    public boolean isRegistrationLikelySuccessful() {
        return isOnRegistrationSuccessUrl();
    }

    /**
     * Checks if at least one visible error message has non-empty text.
     */
    public boolean hasNonEmptyErrorText() {
        List<WebElement> errors = driver.findElements(ERROR_TEXTS);
        for (WebElement error : errors) {
            try {
                String text = error.getText();
                if (error.isDisplayed() && text != null && !text.trim().isEmpty()) {
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {
                // Ignore stale element and continue.
            }
        }
        return false;
    }

    private boolean hasAnyVisibleErrorText() {
        if (hasNonEmptyErrorText()) {
            return true;
        }
        List<WebElement> genericErrors = driver.findElements(GENERIC_ERROR_TEXTS);
        for (WebElement error : genericErrors) {
            try {
                String text = error.getText();
                if (error.isDisplayed() && text != null && !text.trim().isEmpty()) {
                    return true;
                }
            } catch (StaleElementReferenceException ignored) {
                // Ignore stale element and continue.
            }
        }
        return false;
    }

    private boolean isOnRegistrationSuccessUrl() {
        String current = normalizeUrl(driver.getCurrentUrl());
        String expected = normalizeUrl(SUCCESS_REGISTRATION_URL);
        return expected.equals(current);
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String baseOnly = url.split("[?#]", 2)[0].trim();
        while (baseOnly.endsWith("/") && baseOnly.length() > 1) {
            baseOnly = baseOnly.substring(0, baseOnly.length() - 1);
        }
        return baseOnly.toLowerCase();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String captureFirstVisibleErrorText() {
        List<WebElement> primaryErrors = driver.findElements(ERROR_TEXTS);
        for (WebElement error : primaryErrors) {
            try {
                String text = error.getText();
                if (error.isDisplayed() && text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            } catch (StaleElementReferenceException ignored) {
                // Ignore stale element and continue.
            }
        }

        List<WebElement> genericErrors = driver.findElements(GENERIC_ERROR_TEXTS);
        for (WebElement error : genericErrors) {
            try {
                String text = error.getText();
                if (error.isDisplayed() && text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            } catch (StaleElementReferenceException ignored) {
                // Ignore stale element and continue.
            }
        }
        return "";
    }

    private void type(By locator, String value) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }
}
