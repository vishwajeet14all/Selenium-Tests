package com.automation.tests;

// =================================================================================================
// Imports:
//   - BaseTest         : provides WebDriver lifecycle and URL navigation for each test.
//   - RegistrationPage : the Page Object that exposes high-level registration actions.
//   - Assert / Test    : TestNG assertions and the @Test annotation that marks test methods.
// =================================================================================================
import com.automation.base.BaseTest;
import com.automation.pages.RegistrationPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TestNG suite covering the registration screen of the application under test.
 *
 * <p>Each {@code @Test} method exercises a single behaviour and is intentionally short
 * so failures are easy to triage. Heavy lifting (locating elements, waiting, typing) is
 * delegated to {@link RegistrationPage}; this class focuses on the test scenario itself.
 *
 * <p>The class extends {@link BaseTest}, which means every test method:
 * <ul>
 *   <li>Starts with a fresh Chrome browser.</li>
 *   <li>Begins from the registration URL configured in {@code config/<env>.properties}.</li>
 *   <li>Has its browser session closed automatically once it finishes (success or failure).</li>
 * </ul>
 */
public class RegistrationTest extends BaseTest {

    /**
     * Happy-path scenario:
     * submit the registration form with valid inputs and confirm that the user is
     * redirected to the email-verification screen showing the same address.
     */
    @Test(description = "Registers a user with valid credentials and displays the verification screen")
    public void validRegistration() {
        RegistrationPage page = new RegistrationPage(driver);
        String email = uniqueEmail(); // ensure the email is fresh for every run
        enterValidDetails(page, email, true);
        page.submitRegistration();
        Assert.assertTrue(page.isVerificationScreenDisplayed(email),
                "The verification screen should show the registered email address.");
    }

    /**
     * Verifies that submitting an entirely empty form is blocked by the UI:
     * the Register button should remain disabled.
     */
    @Test(description = "Keeps registration unavailable while mandatory fields are empty")
    public void emptyFormSubmissionShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        Assert.assertTrue(page.isRegisterButtonDisabled(), "Register should be disabled for an empty form.");
    }

    /**
     * Verifies that an obviously bad email address is flagged by the framework:
     * the email input receives the Angular {@code is-invalid} class.
     */
    @Test(description = "Marks an invalid email address as invalid")
    public void invalidEmailShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterName("Jane", "Automation");
        page.enterEmail("not-an-email");
        Assert.assertTrue(page.isEmailInvalid(), "Invalid email input should have the is-invalid class.");
    }

    /**
     * Verifies the password mismatch rule:
     * even with a valid email and a strong password, differing confirm-password entries
     * keep the Register button disabled.
     */
    @Test(description = "Marks a non-matching confirmation password as invalid")
    public void passwordMismatchShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterName("Jane", "Automation");
        page.enterEmail(uniqueEmail());
        page.selectCountry("India");
        page.enterPassword("StrongPass1!");
        page.enterConfirmPassword("DifferentPass1!");
        page.acceptAgreement();
        Assert.assertTrue(page.isRegisterButtonDisabled(),
                "Register should be disabled when the confirmation password does not match.");
    }

    /**
     * Verifies the password-strength rule:
     * a single weak password should immediately receive the {@code is-invalid} class.
     */
    @Test(description = "Marks a weak password as invalid")
    public void weakPasswordShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterPassword("weak");
        Assert.assertTrue(page.isPasswordInvalid(), "Weak password input should have the is-invalid class.");
    }

    /**
     * Verifies the Terms & Conditions rule:
     * with all other fields valid but the agreement unchecked, Register stays disabled.
     */
    @Test(description = "Keeps registration unavailable until the agreement is accepted")
    public void termsNotAcceptedShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        enterValidDetails(page, uniqueEmail(), false); // skip agreement
        Assert.assertTrue(page.isRegisterButtonDisabled(),
                "Register should be disabled until the agreement is accepted.");
    }

    // =============================================================================================
    // PRIVATE TEST HELPERS
    // =============================================================================================

    /**
     * Fills every mandatory field with valid data and optionally accepts the agreement.
     * Re-used by multiple tests so each scenario only specifies what makes it unique.
     *
     * @param page             the page object driving the form.
     * @param email            unique email to use for this run.
     * @param acceptAgreement  whether to tick the Terms & Conditions checkbox.
     */
    private void enterValidDetails(RegistrationPage page, String email, boolean acceptAgreement) {
        page.enterName("Jane", "Automation");
        page.enterEmail(email);
        page.selectCountry("India");
        page.enterPassword("StrongPass1!");
        page.enterConfirmPassword("StrongPass1!");
        if (acceptAgreement) {
            page.acceptAgreement();
        }
    }

    /**
     * Generates a unique email address using the current epoch milliseconds.
     * Prevents collisions when the suite is re-run against the same environment
     * (which would otherwise reject duplicate-email registrations).
     */
    private String uniqueEmail() {
        return "qa." + System.currentTimeMillis() + "@example.com";
    }
}
