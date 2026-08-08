# Registration Page Automation

Selenium automation assignment implemented with Java 17, Maven, TestNG, Chrome, WebDriverManager, explicit waits, and the Page Object Model (POM).

## Implementation Status

### Implemented

- Maven project using Java 17, Selenium, TestNG, WebDriverManager, and Chrome
- Page Object Model: `BaseTest`, `RegistrationPage`, `RegistrationTest`, and reusable utilities
- Dev and prod URL profiles, with system-property and environment-variable overrides
- Valid registration through the email-verification screen
- Empty-form validation by verifying the Register button is disabled
- Invalid email validation through Angular's `is-invalid` state
- Password mismatch validation by verifying the Register button remains disabled
- Weak-password validation through Angular's `is-invalid` state
- Terms and Conditions validation by verifying the Register button remains disabled
- Explicit waits for page interaction and dynamic validation; no `Thread.sleep()` or implicit wait
- TestNG suite configuration, Surefire/TestNG HTML reports, and automatic failure screenshots
- Local Git repository initialized and pushed to GitHub

### Verified Result

The dev suite was executed with:

```bash
mvn clean test -Denv=dev
```

Result: **6 tests run, 0 failures, 0 errors, 0 skipped.**

### Not Implemented

The assignment sheet lists fields that do not appear in the supplied registration AUT or Cypress selectors. They are intentionally not implemented because no reliable UI locator or behavior is available:

- Organization name
- Phone number and phone-number validation
- Gender selection
- Completing email-code verification; the suite validates the verification screen and prefilled email only

## Project Structure

```
Selenium/
|- pom.xml
|- testng.xml
|- README.md
`- src/test/
   |- java/com/genai/
   |  |- base/BaseTest.java
   |  |- pages/RegistrationPage.java
   |  |- tests/RegistrationTest.java
   |  `- utils/
   |     |- Config.java
   |     `- ScreenshotListener.java
   `- resources/config/
      |- dev.properties
      `- prod.properties
```

### Responsibilities

**BaseTest.java**

* Initializes Chrome WebDriver.
* Opens the configured application URL.
* Provides browser setup and teardown.
* Creates a fresh browser session for each test.

**RegistrationPage.java**

* Contains page locators.
* Contains registration-page actions.
* Handles validation and page-level interactions.
* Keeps Selenium implementation separate from test cases.

**RegistrationTest.java**

* Contains TestNG test scenarios.
* Performs test assertions.
* Uses the Page Object instead of directly interacting with WebElements.

**Config.java**

* Loads environment-specific configuration.
* Supports development and production URLs.
* Supports system-property and environment-variable overrides.


## Prerequisites

- JDK 17 or newer
- Maven 3.8 or newer
- Google Chrome
- Network access on the first run so WebDriverManager can resolve ChromeDriver

## Run

The suite selects the `dev` profile by default. The current dev URL is in `src/test/resources/config/dev.properties`.

```bash
mvn clean test -Denv=dev
```

Select production with a Maven property:

```bash
mvn clean test -Denv=prod
```

Environment variables may override profile URLs without modifying tracked files. On PowerShell:

```powershell
$env:TEST_ENV = "prod"
$env:REGISTRATION_URL_PROD = "https://your-production-registration-page.example"
mvn clean test
```

Configuration precedence is: `-Dregistration.url`, `REGISTRATION_URL_<ENV>`, then `config/<env>.properties`. For example, `REGISTRATION_URL_DEV` overrides the dev profile. If no URL is available, tests are skipped with a clear message.

Run Chrome headlessly when needed:

```bash
mvn clean test -Denv=dev -Dheadless=true
```

## Reports And Evidence

- TestNG and Surefire HTML reports: `target/surefire-reports/`
- Failure screenshots: `target/screenshots/`

## AUT Scope And Locators

`RegistrationPage` uses the supplied AUT selectors: `First Name`, last name, email input, `Country of Nationality`, `Password`, `Confirm Password`, agreement checkbox, the Angular `app-button` Register control, email-verification screen, and `.toast-title` / `.toast-message` success notifications.

`getValidationMessages()` collects visible text from common validation-message patterns: alert roles, Angular Material errors, and common error-message CSS classes. `getToastMessage()` fetches the visible `.toast-message` text, including the production message shown in the supplied screenshot: `"email" must be a valid email`. The dev invalid-email test uses the confirmed Angular `is-invalid` state because its API does not display that toast for the same input.

The supplied Cypress flow does not show a phone-number field. The phone-number validation case was removed rather than retaining a locator that cannot work against this AUT. The production profile currently uses `/auth/login`; change it to the production registration route if login does not redirect to the registration form.

## Design Notes

- Each test receives a clean Chrome session from `BaseTest`.
- `WebDriverWait` is used for interaction and dynamic validation feedback. No `Thread.sleep()` or implicit wait is used.
- `ScreenshotListener` captures the browser state automatically when a test fails.
- Test data generates a unique email address to avoid registration collisions.
