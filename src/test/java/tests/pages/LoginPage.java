package tests.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Login page object for legacy tests package.
 */
public class LoginPage {
    private final WebDriver driver;

    private static final By USERNAME_INPUT = By.id("emailphone");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button[contains(text(),'Log In')]");
    private static final By INVALID_CREDENTIALS = By.xpath("//div[contains(text(),'Invalid password')]");
    private static final By UNREGISTERED_PHONE = By.xpath("//div[contains(text(),'Email or phone is not registered.')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("login");
    }

    public void waitUntilLoaded(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
    }

    public void enterUsername(String username) {
        WebElement usernameElement = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        usernameElement.clear();
        usernameElement.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement passwordElement = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        passwordElement.clear();
        passwordElement.sendKeys(password);
    }

    public void clickLogin() {
        WebElement loginBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        loginBtn.click();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public void waitForDashboard(Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.urlContains("/orders/new"));
    }

    public boolean isDashboardLoaded() {
        return driver.getCurrentUrl().contains("/orders/new")
                && driver.getTitle() != null
                && driver.getTitle().toLowerCase().contains("shipmozo");
    }

    public boolean isLoginFormVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public boolean isInvalidCredentialsVisible(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(INVALID_CREDENTIALS));
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    public boolean isUnregisteredPhoneVisible(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(UNREGISTERED_PHONE));
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }
}
