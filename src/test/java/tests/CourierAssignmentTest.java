package tests;

import com.aventstack.extentreports.ExtentTest;
import java.time.Duration;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.utils.BaseTest;
import utilities.ExtentManager;

/**
 * Legacy courier assignment scenario extracted from LoginTest.
 * Kept non-annotated to preserve current execution behavior.
 */
public class CourierAssignmentTest extends BaseTest {

    public CourierAssignmentTest() {
        super();
    }

    public CourierAssignmentTest(String browser) {
        super(browser);
    }

    @Test(enabled = false, groups = {"regression", "legacy"})
    public void assignCourier() {
        ExtentTest test = extent.createTest("Assigning Courier - " + browser).assignCategory(browser);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            wait.until(ExpectedConditions.urlContains("/orders/new"));
            test.pass("New Orders page loaded");
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "NewOrders_" + browser));

            WebElement assignCourierButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Assign Courier']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", assignCourierButton);
            assignCourierButton.click();

            wait.until(ExpectedConditions.urlContains("/orders/assign-courier"));
            test.pass("Assign Courier page loaded");
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "AssignCourier_" + browser));

            String parentTab = driver.getWindowHandle();
            ((JavascriptExecutor) driver)
                    .executeScript("window.open('https://panel.shipmozo.com/tools/rate-calculator', '_blank');");
            test.pass("Opened Rate Calculator in new tab");

            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(parentTab)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            test.pass("Switched to Rate Calculator tab");

            type(By.id("originPincode"), "400012");
            type(By.id("deliveryAreaPincode"), "122001");
            type(By.id("approximateWeight"), "0.5");
            type(By.id("invoiceValue"), "100");
            scrollToElementAndFill("lengthCM-0", "10");
            scrollToElementAndFill("widthCM-0", "10");
            scrollToElementAndFill("heightCM-0", "10");
            test.pass("Filled rate calculator form");

            WebElement calcBtn = wait
                    .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Calculate')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", calcBtn);
            calcBtn.click();
            test.pass("Clicked Calculate");
            ExtentManager.wait(5);
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "RateCalc_Result_" + browser));

            driver.switchTo().window(parentTab);
            test.pass("Switched back to Assign Courier tab");
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "BackToCourier_"));

            WebElement shipNowButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Ship Now']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", shipNowButton);
            shipNowButton.click();

            WebElement pickupsMenu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//p[normalize-space()='Pickups & Manifests']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", pickupsMenu);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", pickupsMenu);

            wait.until(ExpectedConditions.urlContains("/orders/pickup"));
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "Pickups"));

            WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//button[contains(@class,'shipmozo-1m9p94d')])[2]")));
            dropdownButton.click();
            WebElement actionItem = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[normalize-space()='Create Label']")));
            actionItem.click();
            ExtentManager.wait(5);
        } catch (Exception exception) {
            test.fail("Test failed due to: " + exception.getMessage())
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "CourierAssign_Failed_" + browser));
            Assert.fail("AssignCourier failed: " + exception.getMessage());
        }
    }
}
