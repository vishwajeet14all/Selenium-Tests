package com.automation.utils;

// =================================================================================================
// Imports:
//   - BaseTest                      : needed to retrieve the live WebDriver from the failing test.
//   - commons-io FileUtils          : convenience helper for copying files between paths.
//   - Selenium OutputType/TakesScreenshot : APIs for capturing the browser viewport to a file.
//   - TestNG ITestListener/ITestResult : hooks into TestNG's test lifecycle events.
// =================================================================================================
import com.automation.base.BaseTest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TestNG listener that automatically captures a PNG screenshot whenever a test fails.
 *
 * <p>Why it exists:
 * <ul>
 *   <li>Speeds up triage of failed CI runs — the visual state of the browser at the
 *       moment of failure is preserved on disk.</li>
 *   <li>Avoids polluting test code with try/catch blocks dedicated to taking screenshots.</li>
 * </ul>
 *
 * <p>Registered globally via {@code testng.xml} (see the {@code <listeners>} block)
 * so every test class automatically gets the behavior.
 */
public class ScreenshotListener implements ITestListener {

    /**
     * Called by TestNG immediately after a test method fails.
     * <p>The implementation:
     * <ol>
     *   <li>Checks that the failing test actually extends {@link BaseTest} (it must, to
     *       have a WebDriver available).</li>
     *   <li>Builds a unique filename: {@code <testMethodName>_<epochMillis>.png}.</li>
     *   <li>Ensures {@code target/screenshots/} exists.</li>
     *   <li>Captures the browser viewport and copies it into the target folder.</li>
     *   <li>Logs (but does not rethrow) any IO error so a screenshot problem never
     *       masks the original test failure.</li>
     * </ol>
     */
    @Override
    public void onTestFailure(ITestResult result) {
        // Pattern-match: bail out gracefully if the test class isn't a BaseTest
        // (which means no WebDriver to capture) or the driver was never created.
        if (!(result.getInstance() instanceof BaseTest test) || test.getDriver() == null) {
            return;
        }

        // Build a unique filename so simultaneous failures cannot overwrite each other.
        // test method name keeps the screenshot traceable; currentTimeMillis() guarantees uniqueness.
        String fileName = result.getMethod().getMethodName() + "_" + System.currentTimeMillis() + ".png";
        Path destination = Path.of("target", "screenshots", fileName);
        try {
            // Create the screenshots folder if it is missing (e.g. on a fresh CI agent).
            Files.createDirectories(destination.getParent());
            // Cast to TakesScreenshot, ask Selenium for a temporary PNG file, and copy
            // it to our destination path using commons-io for convenience.
            FileUtils.copyFile(
                    ((TakesScreenshot) test.getDriver()).getScreenshotAs(OutputType.FILE),
                    destination.toFile()
            );
        } catch (IOException exception) {
            // Never rethrow — screenshot capture is best-effort and must not hide the
            // actual test failure that we are trying to investigate.
            System.err.println("Unable to save failure screenshot: " + exception.getMessage());
        }
    }
}
