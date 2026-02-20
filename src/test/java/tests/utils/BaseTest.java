package tests.utils;

import com.aventstack.extentreports.ExtentReports;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Shared base for legacy UI tests in tests package.
 * Keeps setup/teardown and small UI helpers in one place.
 */
public abstract class BaseTest {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    protected WebDriver driver;
    protected ExtentReports extent;
    protected String browser;

    protected BaseTest() {
        this("chrome");
    }

    protected BaseTest(String browser) {
        this.browser = (browser == null || browser.isBlank()) ? "chrome" : browser.toLowerCase();
    }

    /**
     * Creates WebDriver and ExtentReports before test class execution.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        extent = ExtentManager.getInstance(getClass().getSimpleName());
        try {
            System.out.println(">>> Browser Param: " + browser);
            switch (browser) {
                case "chrome":
                    ChromeOptions options = new ChromeOptions();
                    // options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(options);
                    driver.manage().window().maximize();
                    // driver.manage().window().setSize(new Dimension(1366, 768));
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.addArguments("--no-sandbox");
                    firefoxOptions.addArguments("--disable-dev-shm-usage");
                    driver = new FirefoxDriver(firefoxOptions);
                    driver.manage().window().setSize(new Dimension(1366, 768));
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver();
                    driver.manage().window().setSize(new Dimension(1366, 768));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported browser: " + browser);
            }
        } catch (Exception exception) {
            System.out.println("Failed to start browser: " + browser + " - " + exception.getMessage());
            Assert.fail("Driver setup failed for browser: " + browser, exception);
        }
    }

    /**
     * Flushes report and closes browser after class execution.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (extent != null) {
            extent.flush();
        }
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Scrolls to a sidebar-like section label and clicks it when present.
     */
    protected void expandSection(String sectionName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement section = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p[contains(text(), '" + sectionName + "')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", section);
            section.click();
        } catch (Exception exception) {
            System.out.println("Unable to expand section '" + sectionName + "': " + exception.getMessage());
        }
    }

    /**
     * Scrolls to an element by id and fills text.
     */
    protected void scrollToElementAndFill(String elementId, String value) {
        WebElement element = driver.findElement(By.id(elementId));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        new WebDriverWait(driver, DEFAULT_TIMEOUT).until(ExpectedConditions.elementToBeClickable(element));
        element.clear();
        element.sendKeys(value);
    }

    /**
     * Scrolls to an element and clicks it.
     */
    protected void scrollToElementAndClick(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', behavior:'smooth'});",
                element);
        ExtentManager.wait(1);
        element.click();
    }

    /**
     * Waits until the element is visible and returns it.
     */
    protected WebElement waitVisible(By locator) {
        return new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until element is clickable and clicks it.
     */
    protected void click(By locator) {
        WebElement element = new WebDriverWait(driver, DEFAULT_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    /**
     * Waits for visible element, clears it, then types value.
     */
    protected void type(By locator, String value) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(value);
    }
}
