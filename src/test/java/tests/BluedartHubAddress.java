package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;
import java.time.Duration;
import java.util.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.ExtentManager;

public class BluedartHubAddress {

	WebDriver driver;
    ExtentReports extent;
    ExtentTest test;
	String browser;
	private String prevPickupAddr = "";
	private String prevDeliveryAddr = "";
    String csvPath;
	int startRow;

//	private static final String MASTER_FILE = "src/test/PincodeMaster.csv";

	public BluedartHubAddress(String browser, String csvPath, int startRow) {
	    this.browser = browser;
	    this.csvPath = csvPath;
	    this.startRow = startRow;
	}
	
	private void updateMasterCSVInPlace(String masterFilePath, List<String[]> updatedRows) {
	    try {
	        File masterFile = new File(masterFilePath);
	        List<String[]> masterRows = new ArrayList<>();

	        // Read existing
	        if (masterFile.exists()) {
	            try (CSVReader reader = new CSVReader(new FileReader(masterFile))) {
	                masterRows = reader.readAll();
	            }
	        }

	        // Index for lookup
	        Map<String, Integer> pincodeToIndex = new HashMap<>();
	        for (int i = 1; i < masterRows.size(); i++) {
	            String[] row = masterRows.get(i);
	            if (row.length > 0) {
	                pincodeToIndex.put(row[0].trim(), i);
	            }
	        }

	        // Update or insert
	        for (String[] newRow : updatedRows) {
	            String pincode = newRow[0].trim();
	            if (pincodeToIndex.containsKey(pincode)) {
	                int idx = pincodeToIndex.get(pincode);
	                masterRows.set(idx, newRow);
	            } else {
	                masterRows.add(newRow);
	            }
	        }

	        // Write back to same file
	        try (CSVWriter writer = new CSVWriter(new FileWriter(masterFile))) {
	            writer.writeAll(masterRows);
	        }

	        System.out.println("📌 Master CSV updated in-place successfully with " + updatedRows.size() + " rows.");

	    } catch (IOException | CsvException e) {
	        System.err.println("❌ Failed to update master file in-place: " + e.getMessage());
	    }
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
                this.driver = new ChromeDriver();        // ✅ Assign to the class variable
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
        System.out.println(" Failed to start browser: " + browser + " — " + e.getMessage());
        Assert.fail("Driver setup failed for browser: " + browser, e);
    }	
    }

    
    
    @Test(priority = 1)
    public void PincodeCSVUpdater() {
        String workingFilePath = this.csvPath;
        List<String[]> batchBuffer = new ArrayList<>();
        String masterFilePath = "src/test/PincodeMaster.csv"; // 🔁 Change path as needed

        try {
            List<String[]> allRows;
            try (CSVReader reader = new CSVReader(new FileReader(workingFilePath))) {
                allRows = reader.readAll();
            }

            if (allRows.isEmpty()) {
                System.out.println("❌ CSV is empty.");
                return;
            }

            driver.get("https://www.bluedart.com/home#locationfinder");
            
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cookiebar-coo-close")
                ));
                okButton.click();
                System.out.println("✅ Cookie alert dismissed.");
                Thread.sleep(500); // Optional short delay
            } catch (Exception e) {
                System.out.println("⚠️ Cookie alert not found or already dismissed.");
            }

            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("pickupRadio")));

            int processedCount = 0;

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                // 🛑 Skip rows before configured startRow
                if (i < this.startRow) {
                    continue;
                }

                String pincode = row[0].trim();
                boolean pickupBlank = row.length < 2 || row[1].trim().isEmpty();
                boolean deliveryBlank = row.length < 3 || row[2].trim().isEmpty();

                if (!pickupBlank && !deliveryBlank) {
                    System.out.println("⏭️ Skipping row " + (i + 1) + ": Already filled for " + pincode);
                    continue;
                }

                String[] addressData = getAddressByPincode(pincode, i);
                String pickup = addressData[0];
                String delivery = addressData[1];
                String pickupError = addressData[2];
                String deliveryError = addressData[3];

                if (row.length < 6) {
                    row = Arrays.copyOf(row, 6);
                }

                row[1] = pickup;
                row[2] = delivery;
                row[3] = pickupError;
                row[4] = deliveryError;
                row[5] = "DONE";

                allRows.set(i, row);
                batchBuffer.add(row);
                processedCount++;

                System.out.println("✅ Updated row " + (i + 1) + ": " + pincode);

                if (processedCount % 10 == 0) {
                    workingFilePath = saveToCSVWithBackup(workingFilePath, allRows);
                    updateMasterCSVInPlace(masterFilePath, batchBuffer);
                    batchBuffer.clear();
                }
            }

            // Final batch flush
            workingFilePath = saveToCSVWithBackup(workingFilePath, allRows);
            if (!batchBuffer.isEmpty()) {
            	updateMasterCSVInPlace(masterFilePath, batchBuffer);
            }

            System.out.println("\n✅ CSV file updated successfully at: " + workingFilePath);
            System.out.println("Total records processed: " + processedCount);
            Runtime.getRuntime().exec("cmd /c start \"\" \"" + workingFilePath + "\"");

        } catch (IOException | CsvException e) {
            e.printStackTrace();
            System.err.println("❌ Error: " + e.getMessage());
        }

        driver.close();
    }



    private String saveToCSVWithBackup(String currentFilePath, List<String[]> allRows) {
        try {
            try (CSVWriter writer = new CSVWriter(new FileWriter(currentFilePath))) {
                writer.writeAll(allRows);
                System.out.println("\n💾 CSV saved to: " + currentFilePath);
                return currentFilePath;
            }
        } catch (IOException e) {
            if (e.getMessage().contains("being used by another process")) {
                try {
                    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                    String backupFilePath = currentFilePath.replace(".csv", "_backup_" + timestamp + ".csv");
                    
                    System.out.println("\n⚠️ Original file is locked. Creating backup file: " + backupFilePath);
                    
                    try (CSVWriter writer = new CSVWriter(new FileWriter(backupFilePath))) {
                        writer.writeAll(allRows);
                        System.out.println("✅ Backup file created and data saved to: " + backupFilePath);
                        return backupFilePath;
                    }
                    
                } catch (IOException ex) {
                    System.err.println("❌ Failed to create backup file: " + ex.getMessage());
                    throw new RuntimeException("Could not save data to any file", ex);
                }
            } else {
                System.err.println("❌ Error saving CSV: " + e.getMessage());
                throw new RuntimeException("Error saving CSV", e);
            }
        }
    }
    
    
    
    
    
    
    
    

    
    public String[] getAddressByPincode(String pincode, int i) {
        String pickupAddr = "";
        String deliveryAddr = "";
        String pickupError = "";
        String deliveryError = "";

        String pickupAddrAttempt = "";
        String deliveryAddrAttempt = "";

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            for (int attempt = 1; attempt <= 1; attempt++) {
                // ----- PICKUP -----
            	wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pickupRadio")));
            	driver.findElement(By.id("pickupRadio")).click();
                Thread.sleep(500);
                WebElement searchBox1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("searchArea")));
                searchBox1.clear();
                searchBox1.sendKeys(pincode, Keys.ENTER);
                Thread.sleep(1500);

                pickupAddrAttempt = "";
                try {
                    WebElement err = driver.findElement(By.xpath("//div[contains(@class,'alert-danger')]"));
                    if (err.isDisplayed()) pickupError = mergeError(pickupError, err.getText().trim());
                } catch (Exception ignored) {}
                try {
                    WebElement err = driver.findElement(By.xpath("//div[contains(@class,'alert-success')]"));
                    if (err.isDisplayed()) pickupError = mergeError(pickupError, err.getText().trim());
                } catch (Exception ignored) {}
                try {
                    WebElement cell = driver.findElement(By.xpath("//table[contains(@class,'table-bordered')]//tbody/tr[1]/td[2]"));
                    pickupAddrAttempt = cell.getText().trim().replace("\n", ", ");
                } catch (Exception e) {
                    pickupError = mergeError(pickupError, "Pickup DOM error: " + e.getClass().getSimpleName());
                }

                // ----- DELIVERY -----
                driver.findElement(By.id("deliveryRadio")).click();
                Thread.sleep(500);
                WebElement searchBox2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("searchArea")));
                searchBox2.clear();
                searchBox2.sendKeys(pincode, Keys.ENTER);
                Thread.sleep(1500);

                deliveryAddrAttempt = "";
                try {
                    WebElement err = driver.findElement(By.xpath("//div[contains(@class,'alert-danger')]"));
                    if (err.isDisplayed()) deliveryError = mergeError(deliveryError, err.getText().trim());
                } catch (Exception ignored) {}
                try {
                    WebElement err = driver.findElement(By.xpath("//div[contains(@class,'alert-success')]"));
                    if (err.isDisplayed()) deliveryError = mergeError(deliveryError, err.getText().trim());
                } catch (Exception ignored) {}
                try {
                    WebElement cell = driver.findElement(By.xpath("//table[contains(@class,'table-bordered')]//tbody/tr[1]/td[2]"));
                    deliveryAddrAttempt = cell.getText().trim().replace("\n", ", ");
                } catch (Exception e) {
                    deliveryError = mergeError(deliveryError, "Delivery DOM error: " + e.getClass().getSimpleName());
                }

                // ---- VALIDATION CHECK ----
                boolean pickupDup = pickupAddrAttempt.equals(prevPickupAddr) || pickupAddrAttempt.equals(prevDeliveryAddr);
                boolean deliveryDup = deliveryAddrAttempt.equals(prevPickupAddr) || deliveryAddrAttempt.equals(prevDeliveryAddr);

                boolean bothPresent = !pickupAddrAttempt.isEmpty() && !deliveryAddrAttempt.isEmpty();
                boolean uniqueEnough = !(pickupDup || deliveryDup);

                if (bothPresent && uniqueEnough) {
                    pickupAddr = pickupAddrAttempt;
                    deliveryAddr = deliveryAddrAttempt;
                    break;
                }

                System.out.println("Retrying Row: "+(i+1)+ "   🔁 Retry " + attempt + ": Duplicate or missing address detected...");
                Thread.sleep(1000);
            }

            // Fallback if nothing worked
            pickupAddr = pickupAddr.isEmpty() ? "NA" : pickupAddr;
            deliveryAddr = deliveryAddr.isEmpty() ? "NA" : deliveryAddr;

            if (pickupAddr.equals(prevPickupAddr) || pickupAddr.equals(prevDeliveryAddr)) {
                pickupError = mergeError(pickupError, "POSSIBLE DUPLICATE FROM PREVIOUS ROW");
            }
            if (deliveryAddr.equals(prevPickupAddr) || deliveryAddr.equals(prevDeliveryAddr)) {
                deliveryError = mergeError(deliveryError, "POSSIBLE DUPLICATE FROM PREVIOUS ROW");
            }

        } catch (Exception e) {
            pickupAddr = "NA";
            deliveryAddr = "NA";
            pickupError = mergeError(pickupError, "FATAL EXCEPTION: " + e.getMessage());
            deliveryError = pickupError;
        }

        // Store for next comparison
        prevPickupAddr = pickupAddr;
        prevDeliveryAddr = deliveryAddr;

        return new String[]{pickupAddr, deliveryAddr, pickupError, deliveryError};
    }

    // Helper to accumulate error messages cleanly
    private String mergeError(String existing, String incoming) {
        if (existing == null || existing.isEmpty()) return incoming;
        if (existing.contains(incoming)) return existing;
        return existing + " | " + incoming;
    }

    
    }


