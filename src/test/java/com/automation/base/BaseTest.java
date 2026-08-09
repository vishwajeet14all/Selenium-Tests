package com.automation.base;

// =================================================================================================
// Imports required by this base class.
//   - Config          : utility that resolves the target registration URL from properties/env.
//   - WebDriverManager: third-party library that auto-downloads the ChromeDriver binary
//                       so the project does not hard-code a driver path.
//   - WebDriver/ChromeDriver/ChromeOptions : Selenium API for driving a Chrome browser.
//   - SkipException   : TestNG exception used to skip a test when preconditions are not met.
//   - BeforeMethod / AfterMethod : TestNG lifecycle annotations.
//   - Duration        : modern java.time API used to set timeouts on the WebDriver.
// =================================================================================================
import com.automation.utils.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Abstract base class for every TestNG test class in this automation project.
 *
 * <p>Purpose of this class:
 * <ul>
 *   <li>Centralize WebDriver lifecycle management (setup + teardown) so individual tests
 *       never have to worry about opening/closing the browser.</li>
 *   <li>Provide each test method with a fresh, isolated Chrome session.</li>
 *   <li>Navigate every test to the same starting URL (the registration page).</li>
 *   <li>Expose the active {@link WebDriver} to other components such as the
 *       {@code ScreenshotListener} through {@link #getDriver()}.</li>
 * </ul>
 *
 * <p>The class is declared {@code abstract} because it is meant only to be extended
 * by concrete test classes (for example, {@code RegistrationTest}); it does not
 * contain any @Test methods of its own.
 */
public abstract class BaseTest {

    /** Shared WebDriver instance for the current test method. Initialized in {@link #setUp()}
     *  and reset to {@code null} implicitly when {@link #tearDown()} closes the browser. */
    protected WebDriver driver;

    /**
     * TestNG setup hook that runs <strong>before every {@code @Test} method</strong>.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Verify a registration URL is configured; otherwise skip the test cleanly.</li>
     *   <li>Ask WebDriverManager to download/setup the ChromeDriver binary.</li>
     *   <li>Build Chrome launch options (maximized window, no notification popups).</li>
     *   <li>Optionally enable headless mode for CI/CD when {@code -Dheadless=true} is passed.</li>
     *   <li>Launch ChromeDriver and apply a 30-second page load timeout.</li>
     *   <li>Open the configured registration URL so the test starts at the right page.</li>
     * </ol>
     */
    @BeforeMethod
    public void setUp() {
        // ---- 1. Precondition check -------------------------------------------------
        // Make sure the target URL is provided either via a system property, env var,
        // or config/<env>.properties file. If none is available, fail loudly with a
        // descriptive SkipException instead of opening the browser at the wrong page.
        if (Config.registrationUrl().isBlank()) {
            throw new SkipException("Set the registration page URL with -Dregistration.url=<url>.");
        }

        // ---- 2. Driver binary resolution ------------------------------------------
        // WebDriverManager downloads a ChromeDriver version that matches the locally
        // installed Chrome browser, so we never have to commit a chromedriver.exe.
        WebDriverManager.chromedriver().setup();

        // ---- 3. Chrome launch options ---------------------------------------------
        // Start with the browser window maximized and silence notification popups so
        // they do not interfere with the tests or screenshot output.
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--disable-notifications");

        // ---- 4. Optional headless mode --------------------------------------------
        // Useful when running on a headless build server (e.g. GitHub Actions, Jenkins)
        // or locally with `mvn clean test -Dheadless=true`.
        if (Boolean.parseBoolean(Config.value("headless", "false"))) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }

        // ---- 5. Launch Chrome -----------------------------------------------------
        driver = new ChromeDriver(options);

        // ---- 6. Configure WebDriver timeouts --------------------------------------
        // Limit how long Selenium waits for a page to load. Without this, slow pages
        // could hang a test indefinitely.
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // ---- 7. Navigate to the application under test ---------------------------
        // Every test starts from the same registration page so test setup is consistent.
        driver.get(Config.registrationUrl());
    }

    /**
     * Returns the active WebDriver instance.
     * <p>Primarily used by {@code ScreenshotListener} so it can capture a PNG of the
     * browser whenever a test fails.
     *
     * @return the WebDriver created in {@link #setUp()} for the current test method.
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * TestNG teardown hook that runs <strong>after every {@code @Test} method</strong>.
     * <p>The {@code alwaysRun = true} flag guarantees the browser is closed even if
     * the test itself fails or throws an exception, preventing leaked Chrome sessions.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        // Close every browser window and cleanly terminate the ChromeDriver process.
        // Guarded with a null check in case setup failed before the driver was assigned.
        if (driver != null) {
            driver.quit();
        }
    }
}
