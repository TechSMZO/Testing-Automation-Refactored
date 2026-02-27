package tests.pages;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.time.Duration;
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
    private static final By ATTACH_FILE_INPUT_PROXY = By.xpath(
            "//div[@role='dialog']//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'file')]");
    private static final By ATTACH_FILE_BUTTON = By.xpath(
            "//div[@role='dialog']//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'attach file')]/following::button[1]");
    private static final By ATTACH_FILE_AREA = By.xpath(
            "//div[@role='dialog']//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'select file')]");
    private static final By ATTACH_FILE_ICON_BUTTON = By.xpath(
            "//div[@role='dialog']//*[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'attach file')]/following::button[.//*[name()='svg']][1]");
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

        // Local flow: open native file picker and paste file path.
        uploadUsingSystemDialog(filePath);

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

    private void uploadUsingSystemDialog(String filePath) {
        boolean opened = false;
        if (!driver.findElements(UPLOAD_DIALOG_TITLE).isEmpty()) {
            opened = clickIfPresent(ATTACH_FILE_ICON_BUTTON);
        }
        if (!opened) {
            opened = clickIfPresent(ATTACH_FILE_INPUT_PROXY);
        }
        if (!opened) {
            opened = clickIfPresent(ATTACH_FILE_BUTTON);
        }
        if (!opened) {
            opened = clickIfPresent(ATTACH_FILE_AREA);
        }
        if (!opened) {
            throw new RuntimeException("Attach file control not clickable for native upload. " + uploadDialogSnapshot());
        }
        try {
            Thread.sleep(1000);
            StringSelection selection = new StringSelection(filePath);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            Thread.sleep(500);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            Thread.sleep(1000);
        } catch (Exception exception) {
            throw new RuntimeException("Native file dialog upload failed: " + exception.getMessage(), exception);
        }
    }

    private String uploadDialogSnapshot() {
        StringBuilder sb = new StringBuilder();
        try {
            List<WebElement> dialogs = driver.findElements(By.cssSelector("div[role='dialog']"));
            sb.append("dialogs=").append(dialogs.size()).append("; ");
            if (!dialogs.isEmpty()) {
                String text = dialogs.get(0).getText();
                if (text != null) {
                    String compact = text.replaceAll("\\s+", " ").trim();
                    if (compact.length() > 300) {
                        compact = compact.substring(0, 300) + "...";
                    }
                    sb.append("dialogText=").append(compact).append("; ");
                }
                List<WebElement> buttons = dialogs.get(0).findElements(By.tagName("button"));
                sb.append("buttons=").append(buttons.size()).append("; ");
                for (int i = 0; i < Math.min(buttons.size(), 5); i++) {
                    String bt = buttons.get(i).getText();
                    sb.append("btn[").append(i).append("]=").append(bt == null ? "" : bt.trim()).append("; ");
                }
                List<WebElement> inputs = dialogs.get(0).findElements(By.tagName("input"));
                sb.append("inputs=").append(inputs.size()).append("; ");
                for (int i = 0; i < Math.min(inputs.size(), 8); i++) {
                    WebElement input = inputs.get(i);
                    sb.append("in[").append(i).append("]{type=").append(input.getAttribute("type"))
                            .append(",placeholder=").append(input.getAttribute("placeholder"))
                            .append(",id=").append(input.getAttribute("id")).append("}; ");
                }
            }
        } catch (RuntimeException ignored) {
            sb.append("dialogSnapshotError;");
        }
        return sb.toString();
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
