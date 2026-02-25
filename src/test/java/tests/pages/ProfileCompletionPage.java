package tests.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Profile completion page object for address and document update flow.
 */
public class ProfileCompletionPage {
    private final WebDriver driver;

    private static final By PROFILE_MARKER = By.xpath("//*[contains(normalize-space(),'User Profile')]");
    private static final By MY_PROFILE_TAB = By.xpath("//*[contains(normalize-space(),'My Profile')]");
    private static final By ADDRESS_EDIT_BUTTON = By.xpath("(//*[contains(normalize-space(),'Address Details')]/following::button)[1]");
    private static final By ADDRESS_MODAL_HEADING = By.xpath("//*[contains(normalize-space(),'Edit Address, Company Detail')]");
    private static final By ADDRESS_LINE1 = By.xpath("//input[@placeholder='Flat, House No, Building, Apartment']");
    private static final By ADDRESS_LINE2 = By.xpath("//input[@placeholder='Area, Colony, Street No., Sector']");
    private static final By PINCODE = By.xpath("//input[@placeholder='Pincode']");
    private static final By COMPANY_NAME = By.xpath("//input[@placeholder='Enter company name']");
    private static final By STORE_NAME = By.xpath("//input[@placeholder='Enter store name']");
    private static final By DOCUMENTS_TAB = By.xpath("//*[normalize-space()='Documents']");
    private static final By DOCUMENTS_MARKER = By.xpath("//*[contains(normalize-space(),'Upload / View Documents')]");
    private static final By PAN_UPLOAD_BUTTON = By.xpath(
            "(//*[contains(normalize-space(),'PAN Card/Driving License')]/following::button[normalize-space()='Upload'])[1]");
    private static final By UPLOAD_DIALOG_TITLE = By.xpath("//div[@role='dialog']//*[normalize-space()='Upload document']");
    private static final By DIALOG_DOCUMENT_NAME_INPUT = By.xpath("//div[@role='dialog']//input[@placeholder='Document name']");
    private static final By DOCUMENT_NUMBER_INPUT = By.xpath("//div[@role='dialog']//input[@placeholder='Enter document number']");
    private static final By ATTACH_FILE_INPUT_PROXY = By.xpath("//div[@role='dialog']//input[@placeholder='Select file']");
    private static final By ATTACH_FILE_BUTTON = By.xpath("//div[@role='dialog']//*[normalize-space()='Attach File']/following::button[1]");
    private static final By ATTACH_FILE_AREA = By.xpath("//div[@role='dialog']//*[contains(normalize-space(),'Select file')]");
    private static final By FILE_INPUT_IN_DIALOG = By.xpath("//div[@role='dialog']//input[@type='file']");
    private static final By FILE_INPUT_ANYWHERE = By.xpath("//input[@type='file']");
    private static final By FILE_INPUT_BROAD = By.cssSelector(
            "input[type='file'], input[accept*='image'], input[accept*='pdf'], input[accept*='.png'], input[accept*='.jpg']");
    private static final By SAVE_IN_DIALOG = By.xpath("//div[@role='dialog']//button[normalize-space()='Save']");
    private static final By PAN_NUMBER_CELL = By.xpath(
            "//tr[.//*[contains(normalize-space(),'PAN Card/Driving License')]]/td[2]");
    private static final By SUCCESS_TOAST = By.xpath(
            "//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]");

    public ProfileCompletionPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/profile");
    }

    public void waitUntilLoaded(Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(PROFILE_MARKER));
    }

    public boolean isOnProfilePage() {
        return driver.getCurrentUrl().contains("/profile");
    }

    public void openAddressEditModal() {
        // In some builds/tab states "My Profile" tab is already active or not rendered immediately.
        if (isVisible(MY_PROFILE_TAB, Duration.ofSeconds(3))) {
            clickIfPresent(MY_PROFILE_TAB);
        }
        click(ADDRESS_EDIT_BUTTON);
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(ADDRESS_MODAL_HEADING));
    }

    public void fillAddressAndSave(String addressLine1, String addressLine2, String pincode, String companyName, String storeName) {
        typeIfEditable(ADDRESS_LINE1, addressLine1);
        typeIfEditable(ADDRESS_LINE2, addressLine2);
        typeIfEditable(PINCODE, pincode);

        if (isVisible(COMPANY_NAME, Duration.ofSeconds(2))) {
            typeIfEditable(COMPANY_NAME, companyName);
        }
        if (isVisible(STORE_NAME, Duration.ofSeconds(2))) {
            typeIfEditable(STORE_NAME, storeName);
        }

        boolean clickedSave = clickIfPresent(SAVE_IN_DIALOG);
        if (!clickedSave) {
            throw new RuntimeException("Address save button not found in modal.");
        }

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.invisibilityOfElementLocated(ADDRESS_MODAL_HEADING));
    }

    public void openDocumentsSection() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(DOCUMENTS_TAB));
        boolean clicked = clickIfPresent(DOCUMENTS_TAB);
        if (!clicked) {
            throw new RuntimeException("Documents tab was not found on profile page.");
        }
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("/profile/documents"),
                        ExpectedConditions.visibilityOfElementLocated(DOCUMENTS_MARKER)
                ));
    }

    public void uploadPanDocument(String documentNumber, String filePath) {
        click(PAN_UPLOAD_BUTTON);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(UPLOAD_DIALOG_TITLE));
        assertPanDialogOpened();

        type(DOCUMENT_NUMBER_INPUT, documentNumber);

        // Some builds create file input only after clicking attach area.
        clickIfPresent(ATTACH_FILE_AREA);
        attachFileUsingInput(filePath);

        boolean clickedSave = clickIfPresent(SAVE_IN_DIALOG);
        if (!clickedSave) {
            throw new RuntimeException("Save button not found in upload dialog.");
        }
        // On slower runners, dialog close can lag even after successful submit.
        // Accept either dialog close or a visible success signal.
        waitForUploadCompletion(Duration.ofSeconds(10));
    }

    private void waitForUploadCompletion(Duration timeout) {
        long endTime = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < endTime) {
            if (isSuccessToastVisible()) {
                return;
            }

            if (driver.findElements(UPLOAD_DIALOG_TITLE).isEmpty()) {
                return;
            }

            String dialogError = readDialogErrorText();
            if (!dialogError.isBlank()) {
                throw new RuntimeException("Upload dialog validation error: " + dialogError);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String readDialogErrorText() {
        List<WebElement> errors = driver.findElements(By.xpath(
                "//div[@role='dialog']//*[contains(@class,'Mui-error') or @role='alert']"));
        for (WebElement error : errors) {
            try {
                if (!error.isDisplayed()) {
                    continue;
                }
                String text = error.getText() == null ? "" : error.getText().trim();
                if (!text.isBlank()) {
                    return text;
                }
            } catch (RuntimeException ignored) {
                // Ignore stale dialog nodes and continue.
            }
        }
        return "";
    }

    private void assertPanDialogOpened() {
        List<WebElement> nameInputs = driver.findElements(DIALOG_DOCUMENT_NAME_INPUT);
        if (nameInputs.isEmpty()) {
            return;
        }
        String documentName = nameInputs.get(0).getAttribute("value");
        if (documentName == null || !documentName.toLowerCase().contains("pan")) {
            throw new RuntimeException("PAN upload dialog not opened. Current document: " + documentName);
        }
    }

    private void attachFileUsingInput(String filePath) {
        long endTime = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
        RuntimeException lastException = null;

        while (System.currentTimeMillis() < endTime) {
            if (trySendFileInCurrentContext(filePath)) {
                return;
            }
            if (trySendFileInFrames(filePath)) {
                return;
            }

            // Re-open attach trigger so frameworks can recreate the file input.
            clickIfPresent(ATTACH_FILE_INPUT_PROXY);
            clickIfPresent(ATTACH_FILE_BUTTON);
            clickIfPresent(ATTACH_FILE_AREA);

            try {
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (lastException != null) {
            throw new RuntimeException("Failed to upload via input[type=file]: " + lastException.getMessage(), lastException);
        }
        throw new RuntimeException("Failed to upload: no usable input[type=file] found in upload dialog. URL: "
                + driver.getCurrentUrl());
    }

    private boolean trySendFileInCurrentContext(String filePath) {
        List<WebElement> fileInputs = new ArrayList<>();
        fileInputs.addAll(driver.findElements(FILE_INPUT_IN_DIALOG));
        fileInputs.addAll(driver.findElements(FILE_INPUT_ANYWHERE));
        fileInputs.addAll(driver.findElements(FILE_INPUT_BROAD));

        for (WebElement fileInput : fileInputs) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].style.display='block'; arguments[0].style.visibility='visible';"
                                + "arguments[0].style.opacity=1; arguments[0].removeAttribute('hidden');"
                                + "arguments[0].removeAttribute('disabled');",
                        fileInput);
                fileInput.sendKeys(filePath);
                return true;
            } catch (RuntimeException ignored) {
                // Try next candidate.
            }
        }
        return false;
    }

    private boolean trySendFileInFrames(String filePath) {
        List<WebElement> frames = driver.findElements(By.cssSelector("iframe, frame"));
        for (WebElement frame : frames) {
            try {
                driver.switchTo().frame(frame);
                if (trySendFileInCurrentContext(filePath)) {
                    driver.switchTo().defaultContent();
                    return true;
                }
            } catch (RuntimeException ignored) {
                // Ignore invalid frame and continue.
            } finally {
                driver.switchTo().defaultContent();
            }
        }
        return false;
    }


    public boolean isPanNumberUpdated(String documentNumber, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(driverInstance -> {
                List<WebElement> numberCells = driverInstance.findElements(PAN_NUMBER_CELL);
                if (numberCells.isEmpty()) {
                    return false;
                }
                String text = numberCells.get(0).getText() == null ? "" : numberCells.get(0).getText().trim();
                if (text.isEmpty() || "--".equals(text)) {
                    return false;
                }
                return text.equals(documentNumber) || text.contains(documentNumber);
            });
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public boolean isSuccessToastVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_TOAST));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void type(By locator, String value) {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }

    private void typeIfEditable(By locator, String value) {
        List<WebElement> elements = driver.findElements(locator);
        if (elements.isEmpty()) {
            return;
        }

        WebElement element = elements.get(0);
        if (!element.isDisplayed() || !element.isEnabled()) {
            return;
        }

        type(locator, value);
    }

    private void click(By locator) {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(locator));
        try {
            element.click();
        } catch (RuntimeException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private boolean clickIfPresent(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        if (elements.isEmpty()) {
            return false;
        }
        for (WebElement element : elements) {
            if (!element.isDisplayed()) {
                continue;
            }
            try {
                element.click();
                return true;
            } catch (RuntimeException ignored) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    return true;
                } catch (RuntimeException ignoredAgain) {
                    // Try next visible element.
                }
            }
        }
        return false;
    }

    private void waitAndClick(By locator, Duration timeout) {
        new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
        if (!clickIfPresent(locator)) {
            throw new RuntimeException("Could not click visible element for locator: " + locator);
        }
    }

    private boolean isVisible(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
