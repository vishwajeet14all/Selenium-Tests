# Registration Page Automation

Selenium automation assignment implemented with Java 17, Maven, TestNG, Chrome, WebDriverManager, explicit waits, and the Page Object Model (POM).

## Coverage

- Valid registration through the email-verification screen
- Empty mandatory fields
- Invalid email format
- Password mismatch
- Weak password
- Terms and Conditions not accepted
- Failure screenshots and TestNG HTML reports

## Project Structure

```
Selenium/
|- pom.xml
|- testng.xml
|- README.md
`- src/test/java/com/genai/
   |- base/BaseTest.java
   |- pages/RegistrationPage.java
   |- tests/RegistrationTest.java
   `- utils/
      |- Config.java
      `- ScreenshotListener.java
```

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

## AUT Locator Configuration

`RegistrationPage` now uses the supplied AUT selectors: `First Name`, email input, `Country of Nationality`, `Password`, `Confirm Password`, agreement checkbox, the Angular `app-button` register control, and the email-verification screen.

The supplied Cypress flow does not show a phone-number field. The phone-number validation case was removed rather than retaining a locator that cannot work against this AUT. The production profile currently uses `/auth/login`; change it to the production registration route if login does not redirect to the registration form.

## Design Notes

- Each test receives a clean Chrome session from `BaseTest`.
- `WebDriverWait` is used for interaction and dynamic validation feedback. No `Thread.sleep()` or implicit wait is used.
- `ScreenshotListener` captures the browser state automatically when a test fails.
- Test data generates a unique email address to avoid registration collisions.
