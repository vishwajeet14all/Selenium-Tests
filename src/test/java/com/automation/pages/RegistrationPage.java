package com.automation.pages;

// =================================================================================================
// Selenium imports for locating elements, executing JavaScript, sending keystrokes,
// waiting for conditions, and the WebDriver/WebElement contracts used throughout.
// =================================================================================================
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the application-under-test's registration screen.
 *
 * <p>What "Page Object" means here:
 * <ul>
 *   <li>Every CSS/XPath selector lives in one place — this file.</li>
 *   <li>Tests call high-level methods (e.g. {@code enterEmail}, {@code submitRegistration})
 *       and never touch locators directly.</li>
 *   <li>If the UI changes, only this file needs to be updated; tests stay the same.</li>
 * </ul>
 *
 * <p>Design choices:
 * <ul>
 *   <li>{@link WebDriverWait} (10s) is used everywhere instead of {@code Thread.sleep()}
 *       so waits stay fast and reliable.</li>
 *   <li>Public methods represent user-facing actions; private helpers hide Selenium
 *       implementation details such as clearing a field, clicking, or constructing XPath
 *       literals.</li>
 * </ul>
 */
public class RegistrationPage {

    /** Single WebDriverWait reused across all actions on this page (10s timeout). */
    private final WebDriverWait wait;

    /** Reference to the live WebDriver used to interact with the browser. */
    private final WebDriver driver;

    // ---------------------------------------------------------------------------------------------
    // Locators. Defined once at the top so they are easy to find and update.
    // Each locator uses the CSS selector pattern supplied by the AUT's source.
    // ---------------------------------------------------------------------------------------------

    /** First name input — identified by its placeholder text. */
    private final By firstName = By.cssSelector("input[placeholder='First Name']");

    /** Last name input — Angular's nested invalid-form container structure. */
    private final By lastName = By.cssSelector(":nth-child(2) > .ng-invalid > .form-control");

    /** Email input — standard HTML5 email type. */
    private final By email = By.cssSelector("input[type='email']");

    /** Country of Nationality autocomplete input (opens a dropdown on click). */
    private final By country = By.cssSelector("input[placeholder='Country of Nationality']");

    /** Password input. */
    private final By password = By.cssSelector("input[placeholder='Password']");

    /** Confirm password input — must match the password field. */
    private final By confirmPassword = By.cssSelector("input[placeholder='Confirm Password']");

    /** Terms & Conditions agreement checkbox. */
    private final By agreement = By.cssSelector("input[type='checkbox']");

    /** Submit "Register" button — wrapped in Angular's app-button component. */
    private final By registerButton = By.cssSelector("app-button > .form-group > :nth-child(1)");

    /** Generic validation-message locator — covers multiple frameworks (Material, Bootstrap, custom). */
    private final By validationMessages = By.cssSelector(
            "[role='alert'], .invalid-feedback, .error-message, .validation-error, mat-error"
    );

    /** Heading text on the post-submit email verification screen. */
    private final By verificationInstruction = By.cssSelector(
            ".pb-13.font-weight-bolder.text-dark.font-size-h4.font-size-h1-lg"
    );

    /** Read-only email field on the verification screen showing the registered address. */
    private final By verificationEmail = By.id("emailForVerifyCode");

    /** Toast notification title element (top-right of the viewport). */
    private final By toastTitle = By.cssSelector(".toast-title");

    /** Toast notification body text element. */
    private final By toastMessage = By.cssSelector(".toast-message");

    /**
     * Constructs the page object and prepares its WebDriverWait instance.
     *
     * @param driver active WebDriver created by {@code BaseTest}.
     */
    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // =============================================================================================
    // PUBLIC ACTIONS — one method per logical step a user would perform on the page.
    // Tests should call only these methods, never the private helpers below.
    // =============================================================================================

    /** Fills in both first and last name inputs. */
    public void enterName(String firstNameValue, String lastNameValue) {
        type(firstName, firstNameValue);
        type(lastName, lastNameValue);
    }

    /** Clears and types into the email input. */
    public void enterEmail(String emailAddress) {
        type(email, emailAddress);
    }

    /**
     * Selects a country from the autocomplete dropdown.
     *
     * @param countryName exact display name of the country to choose.
     */
    public void selectCountry(String countryName) {
        click(country);
        // Click the dropdown option whose text exactly matches the requested country.
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[normalize-space()=" + xpathLiteral(countryName) + "]")
        )).click();
    }

    /** Clears and types into the password input. */
    public void enterPassword(String passwordValue) {
        type(password, passwordValue);
    }

    /** Clears and types into the confirm-password input. */
    public void enterConfirmPassword(String passwordValue) {
        type(confirmPassword, passwordValue);
    }

    /**
     * Toggles the agreement checkbox ON.
     * Uses JavaScript click to bypass overlay issues common with custom-styled checkboxes.
     */
    public void acceptAgreement() {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(agreement));
        if (!checkbox.isSelected()) {
            // JS click is more reliable than .click() when the visible label intercepts events.
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        }
    }

    /** Clicks the "Register" button to submit the form. */
    public void submitRegistration() {
        click(registerButton);
    }

    // =============================================================================================
    // PUBLIC STATE QUERIES — used by tests to assert the page's current behavior.
    // =============================================================================================

    /**
     * @return true once the Register button transitions to the disabled state.
     *         Used to assert that validation prevents submission.
     */
    public boolean isRegisterButtonDisabled() {
        return wait.until(webDriver -> !webDriver.findElement(registerButton).isEnabled());
    }

    /** @return true when Angular has marked the email input as invalid (CSS class "is-invalid"). */
    public boolean isEmailInvalid() {
        return hasClass(email, "is-invalid");
    }

    /** @return true when Angular has marked the password input as invalid (CSS class "is-invalid"). */
    public boolean isPasswordInvalid() {
        return hasClass(password, "is-invalid");
    }

    /**
     * @param emailAddress email used during registration (for cross-check with the verification screen).
     * @return true if the verification screen is visible AND it shows the same email.
     */
    public boolean isVerificationScreenDisplayed(String emailAddress) {
        String instruction = wait.until(ExpectedConditions.visibilityOfElementLocated(verificationInstruction)).getText();
        String registeredEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(verificationEmail)).getAttribute("value");
        // Both conditions must hold: the heading text AND the prefilled email value.
        return instruction.contains("Please verify your account using the authentication code sent to your email")
                && emailAddress.equals(registeredEmail);
    }

    /**
     * Collects every currently-visible validation message on the page.
     *
     * @return non-blank list of message texts (empty if no validation errors are shown).
     */
    public List<String> getValidationMessages() {
        return driver.findElements(validationMessages).stream()
                .filter(WebElement::isDisplayed)        // ignore hidden elements
                .map(WebElement::getText)               // pull text content
                .filter(message -> !message.isBlank())  // skip empty placeholders
                .toList();
    }

    /** @return true when the toast title contains the expected text. */
    public boolean isToastTitleDisplayed(String expectedText) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastTitle))
                .getText().contains(expectedText);
    }

    /** @return true when the toast message contains the expected text. */
    public boolean isToastMessageDisplayed(String expectedText) {
        return getToastMessage().contains(expectedText);
    }

    /** @return full text of the currently visible toast message. */
    public String getToastMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }

    // =============================================================================================
    // PRIVATE HELPERS — Selenium plumbing kept hidden from the test layer.
    // =============================================================================================

    /** Waits for an element to expose the supplied CSS class (used to detect invalid inputs). */
    private boolean hasClass(By locator, String className) {
        return wait.until(ExpectedConditions.attributeContains(locator, "class", className));
    }

    /**
     * Standardized "type into a field" workflow:
     * wait until clickable → clear → type → press Tab to trigger blur/validation.
     */
    private void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.clear();
        element.sendKeys(value, Keys.TAB); // Tab blurs the field, forcing Angular to run its validators.
    }

    /** Waits for an element to be clickable and then clicks it. */
    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Wraps a string so it can be embedded inside an XPath expression.
     * Splits on single quotes into the XPath concat() pattern: 'part1' or 'part2'.
     */
    private String xpathLiteral(String value) {
        return "'" + value.replace("'", "') or ('") + "'";
    }
}
