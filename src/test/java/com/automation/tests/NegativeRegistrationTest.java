package com.automation.tests;

// =================================================================================================
// Imports:
//   - BaseTest         : provides WebDriver lifecycle and URL navigation for each test.
//   - RegistrationPage : the Page Object that exposes high-level registration actions.
//   - TestNG           : @Test, @DataProvider, and Assert used to drive parametrized scenarios.
// =================================================================================================
import com.automation.base.BaseTest;
import com.automation.pages.RegistrationPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Data-driven registration scenarios.
 *
 * <p>Purpose:
 * <ul>
 *   <li>Demonstrate TestNG's {@link DataProvider} pattern so the same test logic
 *       runs against many input combinations without copy-pasting test methods.</li>
 *   <li>Cover a wider range of bad inputs for the negative email and weak-password
 *       rules than the single example used in {@link RegistrationTest}.</li>
 * </ul>
 *
 * <p>Each row in a {@code @DataProvider} produces one independent test execution
 * with its own browser session, its own report entry, and its own pass/fail status.
 */
public class NegativeRegistrationTest extends BaseTest {

    /**
     * Provides a set of obviously malformed email addresses.
     * <p>Each row produces one invocation of {@link #invalidEmailShowsValidation(String)}.
     *
     * @return 2D array where each row is {@code [emailValue, humanReadableLabel]}.
     */
    @DataProvider(name = "invalidEmails")
    public static Object[][] invalidEmails() {
        return new Object[][] {
                // { input, description used in the test report }
                {"plaintext",            "missing @ and domain"},
                {"missing-at-sign.com",  "missing @ symbol"},
                {"double@@example.com",  "double @ symbol"},
                {"spaces in@email.com",  "contains whitespace"},
                {"@example.com",         "missing local part"}
        };
    }

    /**
     * Verifies that a representative set of malformed email addresses are all
     * rejected by the form. The data-driven rows make regressions easier to triage
     * because the failing case is reported by index/description in TestNG output.
     *
     * @param email      bad email address to type into the field.
     * @param scenario   short label for the failing case (used in the assertion message).
     */
    @Test(dataProvider = "invalidEmails",
            description = "Marks a variety of malformed email addresses as invalid")
    public void invalidEmailShowsValidation(String email, String scenario) {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterName("Jane", "Automation");
        page.enterEmail(email);
        Assert.assertTrue(page.isEmailInvalid(),
                "Email '" + email + "' (" + scenario + ") should be flagged as invalid.");
    }

    /**
     * Provides a set of passwords that fail the application's strength rules.
     *
     * @return 2D array where each row is {@code [passwordValue, humanReadableLabel]}.
     */
    @DataProvider(name = "weakPasswords")
    public static Object[][] weakPasswords() {
        return new Object[][] {
                {"weak",          "too short, no digits/symbols"},
                {"alllowercase",  "no uppercase / digits / symbols"},
                {"ALLUPPERCASE",  "no lowercase / digits / symbols"},
                {"12345678",      "digits only"}
        };
    }

    /**
     * Verifies that several common weak-password patterns are rejected by the form.
     *
     * @param password weak password value to type into the field.
     * @param scenario short label for the failing case (used in the assertion message).
     */
    @Test(dataProvider = "weakPasswords",
            description = "Marks several weak password patterns as invalid")
    public void weakPasswordShowsValidation(String password, String scenario) {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterPassword(password);
        Assert.assertTrue(page.isPasswordInvalid(),
                "Password '" + password + "' (" + scenario + ") should be flagged as invalid.");
    }
}
