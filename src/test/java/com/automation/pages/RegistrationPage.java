package com.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RegistrationPage {

    private final WebDriverWait wait;
    private final WebDriver driver;

    private final By firstName = By.cssSelector("input[placeholder='First Name']");
    private final By lastName = By.cssSelector(":nth-child(2) > .ng-invalid > .form-control");
    private final By email = By.cssSelector("input[type='email']");
    private final By country = By.cssSelector("input[placeholder='Country of Nationality']");
    private final By password = By.cssSelector("input[placeholder='Password']");
    private final By confirmPassword = By.cssSelector("input[placeholder='Confirm Password']");
    private final By agreement = By.cssSelector("input[type='checkbox']");
    private final By registerButton = By.cssSelector("app-button > .form-group > :nth-child(1)");
    private final By validationMessages = By.cssSelector(
            "[role='alert'], .invalid-feedback, .error-message, .validation-error, mat-error"
    );
    private final By verificationInstruction = By.cssSelector(
            ".pb-13.font-weight-bolder.text-dark.font-size-h4.font-size-h1-lg"
    );
    private final By verificationEmail = By.id("emailForVerifyCode");
    private final By toastTitle = By.cssSelector(".toast-title");
    private final By toastMessage = By.cssSelector(".toast-message");

    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterName(String firstNameValue, String lastNameValue) {
        type(firstName, firstNameValue);
        type(lastName, lastNameValue);
    }

    public void enterEmail(String emailAddress) {
        type(email, emailAddress);
    }

    public void selectCountry(String countryName) {
        click(country);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[normalize-space()=" + xpathLiteral(countryName) + "]")
        )).click();
    }

    public void enterPassword(String passwordValue) {
        type(password, passwordValue);
    }

    public void enterConfirmPassword(String passwordValue) {
        type(confirmPassword, passwordValue);
    }

    public void acceptAgreement() {
        WebElement checkbox = wait.until(ExpectedConditions.presenceOfElementLocated(agreement));
        if (!checkbox.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        }
    }

    public void submitRegistration() {
        click(registerButton);
    }

    public boolean isRegisterButtonDisabled() {
        return wait.until(webDriver -> !webDriver.findElement(registerButton).isEnabled());
    }

    public boolean isEmailInvalid() {
        return hasClass(email, "is-invalid");
    }

    public boolean isPasswordInvalid() {
        return hasClass(password, "is-invalid");
    }

    public boolean isVerificationScreenDisplayed(String emailAddress) {
        String instruction = wait.until(ExpectedConditions.visibilityOfElementLocated(verificationInstruction)).getText();
        String registeredEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(verificationEmail)).getAttribute("value");
        return instruction.contains("Please verify your account using the authentication code sent to your email")
                && emailAddress.equals(registeredEmail);
    }

    public List<String> getValidationMessages() {
        return driver.findElements(validationMessages).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(message -> !message.isBlank())
                .toList();
    }

    public boolean isToastTitleDisplayed(String expectedText) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastTitle))
                .getText().contains(expectedText);
    }

    public boolean isToastMessageDisplayed(String expectedText) {
        return getToastMessage().contains(expectedText);
    }

    public String getToastMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }

    private boolean hasClass(By locator, String className) {
        return wait.until(ExpectedConditions.attributeContains(locator, "class", className));
    }

    private void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.clear();
        element.sendKeys(value, Keys.TAB);
    }

    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private String xpathLiteral(String value) {
        return "'" + value.replace("'", "') or ('") + "'";
    }
}
