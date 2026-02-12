// Placeholder for ExtentManager.java content
package utilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
public class ExtentManager {
    static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("./reports/ExtentReport_" + timestamp + ".html");
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
        }
        return extent;
    }

    public static String captureScreenshot(WebDriver driver, String screenshotName) {
        try {
            String relativePath = "screenshots/" + screenshotName + "_" +
                                  new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".png";
            String dest = System.getProperty("user.dir") + "/reports/" + relativePath;
            File destination = new File(dest);
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (driver instanceof ChromiumDriver) {
                ChromiumDriver chromium = (ChromiumDriver) driver;
                try {
                    Map<String, Object> metrics = chromium.executeCdpCommand(
                        "Page.getLayoutMetrics", new HashMap<>()
                    );
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentSize =
                        (Map<String, Object>) metrics.get("contentSize");
                    int width = ((Number) contentSize.get("width")).intValue();
                    int height = ((Number) contentSize.get("height")).intValue();

                    Map<String, Object> deviceMetrics = new HashMap<>();
                    deviceMetrics.put("width", width);
                    deviceMetrics.put("height", height);
                    deviceMetrics.put("deviceScaleFactor", 1);
                    deviceMetrics.put("mobile", false);
                    chromium.executeCdpCommand("Emulation.setDeviceMetricsOverride", deviceMetrics);

                    Map<String, Object> screenshot = chromium.executeCdpCommand(
                        "Page.captureScreenshot", Map.of("fromSurface", true)
                    );
                    String base64 = (String) screenshot.get("data");
                    byte[] bytes = Base64.getDecoder().decode(base64);
                    FileUtils.writeByteArrayToFile(destination, bytes);
                } finally {
                    chromium.executeCdpCommand("Emulation.clearDeviceMetricsOverride", new HashMap<>());
                }
            } else {
                TakesScreenshot ts = (TakesScreenshot) driver;
                File source = ts.getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(source, destination);
            }
            return relativePath;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    // Optional util to simulate waits
    public static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {}
    }
}
