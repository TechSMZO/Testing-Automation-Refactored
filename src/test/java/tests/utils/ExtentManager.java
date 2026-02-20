package tests.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;

/**
 * Central ExtentReports and screenshot helper for tests package.
 */
public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        return getInstance("TestRun");
    }

    /**
     * Creates one report file per run with format: 1237AM19FEB_ClassName.html
     */
    public static ExtentReports getInstance(String className) {
        if (extent == null) {
            Date now = new Date();
            String timePart = new SimpleDateFormat("hmma", Locale.ENGLISH).format(now).toUpperCase(Locale.ENGLISH);
            String datePart = new SimpleDateFormat("ddMMM", Locale.ENGLISH).format(now).toUpperCase(Locale.ENGLISH);
            String safeClassName = (className == null || className.isBlank()) ? "TestRun" : className.trim();
            String reportFileName = timePart + datePart + "_" + safeClassName + ".html";

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("./reports/" + reportFileName);
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
        }
        return extent;
    }

    public static String captureScreenshot(WebDriver driver, String screenshotName) {
        try {
            String relativePath = "screenshots/" + screenshotName + "_"
                    + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".png";
            String destinationPath = System.getProperty("user.dir") + "/reports/" + relativePath;
            File destination = new File(destinationPath);
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            boolean useFullPageCapture = isCiExecution() && driver instanceof ChromiumDriver;
            if (useFullPageCapture) {
                ChromiumDriver chromium = (ChromiumDriver) driver;
                try {
                    Map<String, Object> metrics = chromium.executeCdpCommand("Page.getLayoutMetrics", new HashMap<>());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentSize = (Map<String, Object>) metrics.get("contentSize");
                    int width = ((Number) contentSize.get("width")).intValue();
                    int height = ((Number) contentSize.get("height")).intValue();

                    Map<String, Object> deviceMetrics = new HashMap<>();
                    deviceMetrics.put("width", width);
                    deviceMetrics.put("height", height);
                    deviceMetrics.put("deviceScaleFactor", 1);
                    deviceMetrics.put("mobile", false);
                    chromium.executeCdpCommand("Emulation.setDeviceMetricsOverride", deviceMetrics);

                    Map<String, Object> screenshot = chromium.executeCdpCommand(
                            "Page.captureScreenshot",
                            Map.of("fromSurface", true));
                    String base64 = (String) screenshot.get("data");
                    byte[] bytes = Base64.getDecoder().decode(base64);
                    FileUtils.writeByteArrayToFile(destination, bytes);
                } finally {
                    chromium.executeCdpCommand("Emulation.clearDeviceMetricsOverride", new HashMap<>());
                }
            } else {
                TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
                File source = screenshotDriver.getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(source, destination);
            }
            return relativePath;
        } catch (IOException exception) {
            return exception.getMessage();
        }
    }

    /**
     * Use full-page screenshot only in CI to avoid local viewport flickering.
     */
    private static boolean isCiExecution() {
        String ciEnv = System.getenv("CI");
        if (ciEnv != null && ciEnv.equalsIgnoreCase("true")) {
            return true;
        }
        String ciProperty = System.getProperty("CI");
        return ciProperty != null && ciProperty.equalsIgnoreCase("true");
    }

    public static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
