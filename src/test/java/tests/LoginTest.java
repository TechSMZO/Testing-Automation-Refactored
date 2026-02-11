// Placeholder for LoginTest.java content
package tests;


import java.time.Duration;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
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
import org.testng.annotations.*;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.ExtentManager;



public class LoginTest {
    WebDriver driver;
    ExtentReports extent;
	String browser;

    
    
    public LoginTest(String browser) {
    	 this.browser = "chrome";
         
	}


  

    @BeforeClass
    public void setUp() {
    	
//    	if(this.browser == null) {
//    		browser = "chrome";
//    	}
//    	
 // ✅ CHANGED: Initialize extent FIRST, before driver setup
 extent = ExtentManager.getInstance();

    	try{
    		System.out.println(">>> Browser Param: " + browser);
        switch (browser.toLowerCase()) {
            case "chrome":
            	ChromeOptions options = new ChromeOptions();
// options.addArguments("--headless=new");
// options.addArguments("--window-size=1920,1080");
// options.addArguments("--disable-gpu");
// options.addArguments("--no-sandbox");
// options.addArguments("--disable-dev-shm-usage");
            WebDriverManager.chromedriver().setup(); // Include this if you're using WebDriverManager
                this.driver = new ChromeDriver(options);        // ✅ Assign to the class variable
                this.driver.manage().window().maximize();
                break;
            case "firefox":
            	WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options2 = new FirefoxOptions();
                options2.addArguments("--no-sandbox");
                options2.addArguments("--disable-dev-shm-usage");
                driver = new FirefoxDriver(options2);
                driver.manage().window().maximize();
                break;
            case "edge":
            	WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                driver.manage().window().maximize();	
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        
    } catch (Exception e) {
        System.out.println("🔥 Failed to start browser: " + browser + " — " + e.getMessage());
        Assert.fail("Driver setup failed for browser: " + browser, e);
    }	
    }


    
    
    
    
 
    
    
    
    

    @Test(groups = {"smoke"}, priority = 1)
    public void loginDemoTest() {
    	
    	
    	ExtentTest test;
    	test = extent.createTest("Login Demo Test - " + browser).assignCategory(browser);
    	// 		Opening the login page and entering the creds 
        driver.get("https://panel.shipmozo.com/login");
        test.pass("Opened Login Page");
        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_page"));
        if (!driver.getCurrentUrl().contains("login")) {
            test.fail("Failed to load login page on " + browser);
            Assert.fail("Login page not loaded.");
        }
        try {
        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        	WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailphone")));
        	emailField.clear();
        	emailField.sendKeys("9076763805");
        	test.pass("Entered Username");
        }
        catch (Exception e) {
        	test.fail("Failed to enter username: " + e.getMessage());
        	test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "username_entry_error"));
        	throw new RuntimeException("Unable to enter username", e);
		} 
        driver.findElement(By.id("password")).sendKeys("12345678");
        test.pass("Entered Password");
        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "logging_in"));
        
        
        
        
        
        
        
//      Logging into panel and validating test case along with loading time of Dashboard if test case passes
        long startTime = System.currentTimeMillis();
        driver.findElement(By.xpath("//button[contains(text(),'Log In')]")).click();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.or(
            	    ExpectedConditions.urlContains("/orders/new"),
            	    ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Invalid credentials')]")),
            	    ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Email or phone is not registered."
            	    		+ "')]"))
            	));        
            
//          Validating the test case is pass or failed after checking login was successful or not 
            String currentUrl = driver.getCurrentUrl();
            String title = driver.getTitle();
            if (currentUrl.contains("/orders/new") && title.contains("Shipmozo")) {
                test.pass("Successfully landed on dashboard");   
                
                // Log duration in console and report
                long endTime = System.currentTimeMillis();   // Calculating the dashboard loading time after clicking on login button
                long durationMillis = endTime - startTime;
                double durationSeconds = durationMillis / 1000.0;
                System.out.println("Login → Dashboard Load Time: " + durationSeconds + " seconds");
                test.info("Login to Dashboard Load Time: " + durationSeconds + " seconds");
                ExtentManager.wait(5); // Just simulate wait for redirect
                test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_success"));
            } else {
                // Screenshot on failure
                test.fail("Login Failed: Either wrong credentials or login error occurred")
                    .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"));
                Assert.fail("Login Failed");
            }

        } catch (TimeoutException e) {
            test.fail("Login failed or took too long !! ")
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed"));
            Assert.fail("Login validation failed due to timeout.");
        }

    }
    
    
       
    
//    @Test(priority = 2)
    public void createSingleB2COrder() {
    	ExtentTest test;
        test = extent.createTest("Create Single B2C Order Test - " + browser);
        
        // Click Add Order button
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement addOrderButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/orders/add']")));
        addOrderButton.click();
        ExtentManager.wait(3);
        wait.until(ExpectedConditions.urlContains("/orders/add"));
        test.pass("Clicked Add Order button");
        
        
        //      Fill Order Form
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
            driver.findElement(By.xpath("//input[@name='paymentType' and @value='1']")).click();
            

            expandSection("Warehouse/Pickup Address");
            ExtentManager.wait(5);
            driver.findElement(By.xpath("//p[span[contains(text(),'(Default)')]]")).click();
            
            
//          expandSection("Weight & Dimensions");
            scrollToElementAndClick(By.xpath("//p[normalize-space(text())='Weight & Dimensions']"));
            ExtentManager.wait(5);
            scrollToElementAndFill("totalWeight", "0.5");
            scrollToElementAndFill("lengthCM-0", "10");
            scrollToElementAndFill("widthCM-0", "10");
            scrollToElementAndFill("heightCM-0", "10");
            
            

            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "form_filled"));
            test.pass("All Form fields filled successfully");
            

            // Click Save Order Button
//            scrollToElementAndClick(By.xpath("/html[1]/body[1]/div[1]/div[3]/div[2]/div[1]/div[1]/div[1]/div[3]/div[1]/div[2]/div[2]/button[2]"));
            WebElement saveButton = driver.findElement(By.xpath("//button[contains(., 'Save') and @type='button']"));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

            test.pass("Clicked Save Order button");
            ExtentManager.wait(5);
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "order_saved"));

        } catch (Exception e) {
            test.fail("Order Creation Failed: " + e.getMessage())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "order_creation_failed"));
            Assert.fail("Order creation failed.");
            System.out.println(e.getMessage());
        }
    }

    
    
    
    
//    @Test(priority = 3)
    public void AssignCourier() {
        ExtentTest test = extent.createTest("Assigning Courier - " + browser).assignCategory(browser);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // Wait for New Orders Page
            wait.until(ExpectedConditions.urlContains("/orders/new"));
            test.pass("New Orders page loaded");
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "NewOrders_" + browser));

            // Click Assign Courier for Ref. ID 123
//            WebElement assignCourierButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
//                "//p[contains(text(),'Ref. ID: 123')]/ancestor::tr/following-sibling::tr[1]//button[contains(., 'Assign Courier')]"
//            )));
//            assignCourierButton.click();
//            test.pass("Clicked Assign Courier for Ref. ID 123");
            
         // Wait for the "Assign Courier" button to be clickable
            WebElement assignCourierButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Assign Courier']")
            ));

            // Scroll into view and click
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", assignCourierButton);
            assignCourierButton.click();
            

            ExtentManager.wait(5);
            // Wait for Assign Courier screen
            wait.until(ExpectedConditions.urlContains("/orders/assign-courier"));
            test.pass("Assign Courier page loaded");
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "AssignCourier_" + browser));

            // Store current tab
            String parentTab = driver.getWindowHandle();

            // Open Rate Calculator in new tab
            ((JavascriptExecutor) driver).executeScript("window.open('https://panel.shipmozo.com/tools/rate-calculator', '_blank');");
            test.pass("Opened Rate Calculator in new tab");

            // Switch to Rate Calculator tab
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(parentTab)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            test.pass("Switched to Rate Calculator tab");

            // Fill Rate Calculator form
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("originPincode"))).sendKeys("400012");
            driver.findElement(By.id("deliveryAreaPincode")).sendKeys("122001");
            driver.findElement(By.id("approximateWeight")).sendKeys("0.5");
            driver.findElement(By.id("invoiceValue")).sendKeys("100");
            scrollToElementAndFill("lengthCM-0", "10");
            scrollToElementAndFill("widthCM-0", "10");
            scrollToElementAndFill("heightCM-0", "10");
            test.pass("Filled rate calculator form");

            // Click Calculate
            WebElement calcBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Calculate')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", calcBtn);
            calcBtn.click();
            test.pass("Clicked Calculate");
            ExtentManager.wait(5);
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "RateCalc_Result_" + browser));

            

            // Switch back to parent tab
            driver.switchTo().window(parentTab);
            test.pass("Switched back to Assign Courier tab");
            ExtentManager.wait(3);
            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "BackToCourier_"));
            
            WebElement shipNowButton = wait.until(ExpectedConditions.elementToBeClickable(
            	    By.xpath("//button[normalize-space()='Ship Now']")
            	));

            	// Scroll to the button to ensure visibility
            	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", shipNowButton);

            	// Click the button
            	shipNowButton.click();
            	
            	
            	
            	WebElement pickupsMenu = wait.until(ExpectedConditions.presenceOfElementLocated(
            	    By.xpath("//p[normalize-space()='Pickups & Manifests']")
            	));

            	// Scroll to the element first
            	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", pickupsMenu);

            	// Pause to let overlays vanish (optional but helpful)
            	Thread.sleep(3000);

            	// Use JS click to bypass overlay issues
            	((JavascriptExecutor) driver).executeScript("arguments[0].click();", pickupsMenu);
            	ExtentManager.wait(5);
            	
            	 wait.until(ExpectedConditions.urlContains("/orders/pickup"));
            	 test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "Pickups"));
            	 
            	 
            	// Step 1: Click dropdown icon
            	 WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
            			 By.xpath("(//button[contains(@class,'shipmozo-1m9p94d')])[2]")
            	 ));
            	 dropdownButton.click();
            	 // Step 2: Click an option from dropdown
            	 WebElement actionItem = wait.until(ExpectedConditions.elementToBeClickable(
            	     By.xpath("//li[normalize-space()='Create Label']")  // or whatever label appears
            	 ));
            	 actionItem.click();
            	 ExtentManager.wait(5);
            

            
        } catch (Exception e) {
            test.fail("Test failed due to: " + e.getMessage())
                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "CourierAssign_Failed_" + browser));
            Assert.fail("AssignCourier failed: " + e.getMessage());
        }
    }

    
    
    @AfterClass
    public void tearDown() {
        extent.flush();
        driver.quit();
    }
    
    
    
    // Utility method placed here for clarity and reuse
    private void expandSection(String sectionName) {
	        try {
	
	        	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	            WebElement section = wait.until(ExpectedConditions.elementToBeClickable(
	                    By.xpath("//p[contains(text(), '" + sectionName + "')]")));
	            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", section);
	            section.click();
	        } catch (Exception e) {
	            System.out.println("Unable to expand section '" + sectionName + "': " + e.getMessage());
	        }
	    }
	    
    
   private void scrollToElementAndFill(String elementId, String value) {
        WebElement element = driver.findElement(By.id(elementId));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(element));
        element.clear();
        element.sendKeys(value);
    }


    private void scrollToElementAndClick(By locator) {
        WebElement element = driver.findElement(locator) ;
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', behavior:'smooth'});", element);
        ExtentManager.wait(1);
        element.click();
    }

    
}



