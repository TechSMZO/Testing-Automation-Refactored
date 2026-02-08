// Placeholder for LoginTest.java content
package tests;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    ExtentTest test;
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
    	try{
    		System.out.println(">>> Browser Param: " + browser);
        switch (browser.toLowerCase()) {
            case "chrome":
            	WebDriverManager.chromedriver().setup(); // Include this if you're using WebDriverManager
                ChromeOptions options = new ChromeOptions();

// REQUIRED for GitHub Actions
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");
options.addArguments("--disable-dev-shm-usage");
options.addArguments("--window-size=1920,1080");
options.addArguments("--disable-gpu");

// Optional but safe
options.addArguments("--remote-allow-origins=*");
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
        
        extent = ExtentManager.getInstance();
    } catch (Exception e) {
        System.out.println("🔥 Failed to start browser: " + browser + " — " + e.getMessage());
        Assert.fail("Driver setup failed for browser: " + browser, e);
    }	
    }


    
    
    
    
 
    
    
    
    

    @Test(priority = 1)
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

    
    
    
    
    
    
    
    
    
    
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
//  @Test
  public void shipwayAutomation() {
	  
	  ExtentTest test = extent.createTest("Login Shipway Panel Test - " + browser).assignCategory(browser);
	  
	  
	  

	  //Login to shipway panel
	    try {
	        driver.get("https://app.shipway.com/merchant.php?dispatch=auth.login_form&return_url=merchant.php");
	        test.pass("Opened Shipway Login Page");
	        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "shipway_login_page"));

	        if (!driver.getCurrentUrl().contains("login")) {
	            test.fail("Login page not loaded correctly on " + browser);
	            Assert.fail("Login page failed to load.");
	        }

	        
	        scrollToElementAndFill("username", "munish@apporio.com");
	        test.pass("Entered Username");
	        scrollToElementAndFill("password", "Apporio@6070");
	        test.pass("Entered Password");
//	        scrollToElementAndClick(By.cssSelector("input.btn.btn-primary-signin[type='submit']"));
	        driver.findElement(By.name("dispatch[auth.login]")).click();

	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	        wait.until(ExpectedConditions.or(
	            ExpectedConditions.urlContains("/merchant.php"),
	            ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Invalid')]")),
	            ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'not registered')]"))
	        ));

	        String currentUrl = driver.getCurrentUrl();
	        if (currentUrl.contains("merchant.php") && driver.getTitle().toLowerCase().contains("administration")) {
	            test.pass("Login successful - Dashboard loaded");
	            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_success_shipway"));
	        } else {
	            test.fail("Login failed - Incorrect credentials or error")
	                .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "login_failed_shipway"));
	            Assert.fail("Shipway Login Failed");
	        }

	    } catch (TimeoutException e) {
	        test.fail("Timeout waiting for login to complete")
	            .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "timeout_login_shipway"));
	        Assert.fail("Timeout during login: " + e.getMessage());
	    } catch (Exception e) {
	        test.fail("Unexpected error: " + e.getMessage())
	            .addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "error_login_shipway"));
	        Assert.fail("Unexpected login failure");
	    }
	    
	    
	    
//	    Navigating to warehouses and editing them 
	    
	    
	    driver.get("https://app.shipway.com/merchant.php?dispatch=companies.warehouse&company_id=32813&items_per_page=100&");	    
//	    try {
//	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
//
//	        // Wait and click on Settings
//	        By settingsLink = By.xpath("//a[contains(@href, 'merchant.php?dispatch=settings')]");
//	        wait.until(ExpectedConditions.elementToBeClickable(settingsLink));
//	        scrollToElementAndClick(settingsLink);
//	        test.pass("Clicked on 'Settings'");
////	        driver.get("https://app.shipway.com/merchant.php?dispatch=companies.warehouse&company_id=32813&items_per_page=100&page=4");
//
//	        // Wait and click on Manage Warehouse
//	        By warehouseLink = By.xpath("//a[contains(@href,'dispatch=companies.warehouse') and contains(text(),'Manage Warehouse')]");
//	        wait.until(ExpectedConditions.elementToBeClickable(warehouseLink));
//	        scrollToElementAndClick(warehouseLink);
//	        test.pass("Clicked on 'Manage Warehouse' successfully");
//	        test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "manage_warehouse_page"));
//
//	    } catch (Exception e) {
//	        test.fail("Failed during navigation to Manage Warehouse: " + e.getMessage());
//	        e.printStackTrace();
//	        try {
//	            test.addScreenCaptureFromPath(ExtentManager.captureScreenshot(driver, "error_manage_warehouse_navigation"));
//	        } catch (Exception ignored) {}
//	        Assert.fail("Navigation to Manage Warehouse failed");
//	    }

	    editAllWarehousesAndLog(test);
	    
  }
  
  
// Edit 10 warehouse rows on each page and update contact info sequentially
  public void editAllWarehousesAndLog(ExtentTest test) {
      try {
          WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
          wait.until(ExpectedConditions.titleContains("Warehouse"));
          List<String> updatedWarehouseIds = new ArrayList<>();

          try {
              ExtentManager.wait(2);
//              int totalPages = 571;
              int updated = 0;

              for (int page = 54; page >= 1; page--) {
              	
              	System.out.println("\n\n\n");
              	System.out.println("----------------------------------------------------------------------------------------------------------");
              	System.out.println("\n\n\n");
              	System.out.println("Entered page loop: Page " + page);
                  test.info("Page " + (page) + " processed, capturing screenshot...");
                  test.addScreenCaptureFromPath(
                      ExtentManager.captureScreenshot(driver, "Page_" + page + "_Updated")
                  );
                  

                  wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                      By.xpath("//table[contains(@class, 'table-middle')]//tbody//tr")
                  ));

                 
                  for (int i = 0; i < 100; i++) {
                      boolean success = false;
                      int retryCount = 0;
                      String targetUrl = "https://app.shipway.com/merchant.php?dispatch=companies.warehouse&company_id=32813&items_per_page=100&page=" + page;
                      driver.get(targetUrl);

                      wait.until(ExpectedConditions.titleContains("Warehouse"));
                      

                      while (!success && retryCount < 3) {
                          try {
                              wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                                  By.xpath("//table[contains(@class, 'table-middle')]//tbody//tr")
                              ));
                              List<WebElement> freshRows = driver.findElements(
                                  By.xpath("//table[contains(@class, 'table-middle')]//tbody//tr")
                              );
                              if (i >= freshRows.size()) break;

                              WebElement row = freshRows.get(i);

                              String warehouseId = row.findElement(By.xpath("./td[1]")).getText().trim();
//                              System.out.println("Page :- "+page+"   Warehouse :-  " + i);
                              
                           // Extract email and phone from Contact Details (5th column)
                              String emailInRow = "";
                              String phoneInRow = "";

                              try {
                                  emailInRow = row.findElement(By.xpath("./td[5]//img[contains(@src,'emailIcon')]/following-sibling::span")).getText().trim();
//                                  System.out.println(emailInRow);
                                  phoneInRow = row.findElement(By.xpath("./td[5]//img[contains(@src,'callIcon')]/following-sibling::span")).getText().replaceAll("[^0-9]", "");
//                                  System.out.println(phoneInRow);
                              } catch (Exception eExtract) {
//                                  System.out.println("Failed to extract email/phone for warehouse ID: " + warehouseId + " - " + eExtract.getMessage());
                              }

                           // Check if already updated
//                              System.out.println(emailInRow.equalsIgnoreCase("rajesh@shipmozo.com") && phoneInRow.equals("918750710656"));
                              if (emailInRow.equalsIgnoreCase("rajesh@shipmozo.com") && phoneInRow.equals("918750710656")) {
//                                  System.out.println("Warehouse ID " + warehouseId + " already updated. Skipping...");
                                  retryCount++;
                                  continue; // skip to next row
                              }
                              
                              List<WebElement> editBtns = row.findElements(By.xpath(".//a[contains(@href, 'updatewarehouse')]"));
                              if (editBtns.isEmpty()) {
                                  System.out.println("Edit button not found for warehouse row index: " + i);
                                  continue; // skip to next row
                              }
                              WebElement editBtn = editBtns.get(0);

                              ((JavascriptExecutor) driver).executeScript(
                                  "arguments[0].scrollIntoView({block: 'center'});", editBtn);
                              ExtentManager.wait(1);
                              ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);

                              wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

                              scrollToElementAndFill("contact_person_name", "Rajesh Kumar");
                              scrollToElementAndFill("company", "Shipmozo");
                              scrollToElementAndFill("email", "rajesh@shipmozo.com");
                              scrollToElementAndFill("jquery-intl-phone", "8750710656");

                              scrollToElementAndClick(By.xpath("//button[contains(text(),'Save')]"));

                              wait.until(ExpectedConditions.urlContains("dispatch=companies.warehouse"));
                              updatedWarehouseIds.add(warehouseId);
                              updated++;
                              test.pass("Updated warehouse ID: " + warehouseId);
                          	System.out.println("\n\n");
                              System.out.println("Page :- "+page+"   Warehouse :-  " + i+"  &  Updated warehouse ID: " + warehouseId);
                              System.out.println("Total warehouse updated till now - " + updated);
                              success = true;
                          } catch (Exception retryEx) {
                              retryEx.printStackTrace();
                              retryCount++;
                              ExtentManager.wait(2);
                              if (retryCount == 3) {
                                  test.warning("Row " + (i + 1) + " failed after 3 attempts: " + retryEx.getMessage());
                              }
                          }
                      }
                  }
                  
                  
                
              }

              try {
                  XSSFWorkbook workbook = new XSSFWorkbook();
                  XSSFSheet sheet = workbook.createSheet("Updated Warehouses");
                  for (int i = 0; i < updatedWarehouseIds.size(); i++) {
                      XSSFRow row = sheet.createRow(i);
                      row.createCell(0).setCellValue(updatedWarehouseIds.get(i));
                  }
                  String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
                  String filename = "updated_warehouses_" + timestamp + ".xlsx";
                  FileOutputStream out = new FileOutputStream(filename);                    
                  workbook.write(out);
                  out.close();
                  workbook.close();
                  test.pass("Warehouse updates logged to Excel.");

              } catch (Exception excelEx) {
                  excelEx.printStackTrace();
                  test.fail("Excel write failed: " + excelEx.getMessage());
              }

          } catch (Exception e) {
              e.printStackTrace();
              test.fail(" Script failed: " + e.getMessage());
          }
      } catch (Exception e) {
          e.printStackTrace();
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



