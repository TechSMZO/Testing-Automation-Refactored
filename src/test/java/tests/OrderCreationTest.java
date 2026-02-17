package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import utilities.ExtentManager;

/**
 * Legacy order creation scenario extracted from LoginTest.
 * Kept non-annotated to preserve current execution behavior.
 */
public class OrderCreationTest extends BaseTest {

    public OrderCreationTest() {
        super();
    }

    public OrderCreationTest(String browser) {
        super(browser);
    }

    @Test(enabled = false, groups = {"regression", "legacy"})
    public void createSingleB2COrder() {
        ExtentTest test = extent.createTest("Create Single B2C Order Test - " + browser);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement addOrderButton = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/orders/add']")));
        addOrderButton.click();
        wait.until(ExpectedConditions.urlContains("/orders/add"));
        test.pass("Clicked Add Order button");

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            scrollToElementAndFill("user-name", "John Doe");
            scrollToElementAndFill("phone", "9876543210");
            scrollToElementAndFill("addressLineOne", "adjkwnfkenkjnkenr");
            scrollToElementAndFill("pincode", "122001");

            expandSection("Order Details");
            ExtentManager.wait(5);
            scrollToElementAndFill("referenceId", "123");
            scrollToElementAndFill("product-name", "Test Product");
            scrollToElementAndFill("qty-0", "1");
            scrollToElementAndFill("unitPrice-0", "500");
            click(By.xpath("//input[@name='paymentType' and @value='1']"));

            expandSection("Warehouse/Pickup Address");
            ExtentManager.wait(5);
            click(By.xpath("//p[span[contains(text(),'(Default)')]]"));

            scrollToElementAndClick(By.xpath("//p[normalize-space(text())='Weight & Dimensions']"));
            ExtentManager.wait(5);
            scrollToElementAndFill("totalWeight", "0.5");
            scrollToElementAndFill("lengthCM-0", "10");
            scrollToElementAndFill("widthCM-0", "10");
            scrollToElementAndFill("heightCM-0", "10");

            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "form_filled"));
            test.pass("All Form fields filled successfully");

            WebElement saveButton = driver.findElement(By.xpath("//button[contains(., 'Save') and @type='button']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

            test.pass("Clicked Save Order button");
            ExtentManager.wait(5);
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "order_saved"));
        } catch (Exception exception) {
            test.fail("Order Creation Failed: " + exception.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "order_creation_failed"));
            Assert.fail("Order creation failed.");
        }
    }
}
