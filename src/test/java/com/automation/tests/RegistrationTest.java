package com.automation.tests;

import com.automation.base.BaseTest;
import com.automation.pages.RegistrationPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RegistrationTest extends BaseTest {

    @Test(description = "Registers a user with valid credentials and displays the verification screen")
    public void validRegistration() {
        RegistrationPage page = new RegistrationPage(driver);
        String email = uniqueEmail();
        enterValidDetails(page, email, true);
        page.submitRegistration();
        Assert.assertTrue(page.isVerificationScreenDisplayed(email),
                "The verification screen should show the registered email address.");
    }

    @Test(description = "Keeps registration unavailable while mandatory fields are empty")
    public void emptyFormSubmissionShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        Assert.assertTrue(page.isRegisterButtonDisabled(), "Register should be disabled for an empty form.");
    }

    @Test(description = "Marks an invalid email address as invalid")
    public void invalidEmailShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterName("Jane", "Automation");
        page.enterEmail("not-an-email");
        Assert.assertTrue(page.isEmailInvalid(), "Invalid email input should have the is-invalid class.");
    }

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

    @Test(description = "Marks a weak password as invalid")
    public void weakPasswordShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        page.enterPassword("weak");
        Assert.assertTrue(page.isPasswordInvalid(), "Weak password input should have the is-invalid class.");
    }

    @Test(description = "Keeps registration unavailable until the agreement is accepted")
    public void termsNotAcceptedShowsValidation() {
        RegistrationPage page = new RegistrationPage(driver);
        enterValidDetails(page, uniqueEmail(), false);
        Assert.assertTrue(page.isRegisterButtonDisabled(),
                "Register should be disabled until the agreement is accepted.");
    }

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

    private String uniqueEmail() {
        return "qa." + System.currentTimeMillis() + "@example.com";
    }
}
