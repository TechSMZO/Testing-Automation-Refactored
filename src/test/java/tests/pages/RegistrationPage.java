package tests.pages;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Registration page object.
 */
public class RegistrationPage {
    private final WebDriver driver;

    private static final By HEADING = By.xpath("//h3[contains(normalize-space(),'Register to Shipmozo')]");
    private static final By USER_TYPE_DROPDOWN = By.id("mui-component-select-usertype");
    private static final By NAME_INPUT = By.id("name");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By CONFIRM_PASSWORD_INPUT = By.id("cpassword");
    private static final By AGREE_TERMS_CHECKBOX = By.name("agreeterms");
    private static final By REGISTER_BUTTON = By.xpath("//button[@type='submit' and contains(.,'Register')]");
    private static final By ERROR_TEXTS = By.cssSelector("p.MuiFormHelperText-root.Mui-error");

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(USER_TYPE_DROPDOWN));
        dropdown.click();

        By optionBy = By.xpath("//li[normalize-space()='" + userType + "']");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionBy));
        option.click();
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
        selectUserType(userType);
        typeName(name);
        typeEmail(email);
        typePhone(phone);
        typePassword(password);
        typeConfirmPassword(confirmPassword);
        setAgreeTerms(agreeTerms);
    }

    public void waitForOutcome(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("/register")),
                hasAnyErrorTextCondition()
        ));
    }

    public boolean isRegistrationLikelySuccessful() {
        String url = driver.getCurrentUrl().toLowerCase();
        return url.contains("/login")
                || url.contains("verify")
                || url.contains("otp")
                || !url.contains("/register");
    }

    public boolean hasNonEmptyErrorText() {
        List<WebElement> errors = driver.findElements(ERROR_TEXTS);
        for (WebElement error : errors) {
            String text = error.getText();
            if (text != null && !text.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void type(By locator, String value) {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }

    private ExpectedCondition<Boolean> hasAnyErrorTextCondition() {
        return driverState -> {
            List<WebElement> errors = driverState.findElements(ERROR_TEXTS);
            for (WebElement error : errors) {
                String text = error.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return true;
                }
            }
            return false;
        };
    }
}
