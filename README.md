# Registration Page Automation

Selenium UI automation framework for testing the Registration Page using **Java 17, Selenium WebDriver, TestNG, Maven, WebDriverManager, and Page Object Model (POM)**.

The framework covers positive, negative, validation, synchronization, reporting, and failure-evidence scenarios.

---

## Tech Stack

* **Language:** Java 17
* **Automation Tool:** Selenium WebDriver
* **Test Framework:** TestNG
* **Build Tool:** Maven
* **Browser:** Google Chrome
* **Driver Management:** WebDriverManager
* **Design Pattern:** Page Object Model (POM)
* **Version Control:** Git / GitHub

---

## Framework Structure

```text
Selenium/
│
├── pom.xml
├── testng.xml
├── README.md
│
└── src/test/
    │
    ├── java/com/genai/
    │   │
    │   ├── base/
    │   │   └── BaseTest.java
    │   │
    │   ├── pages/
    │   │   └── RegistrationPage.java
    │   │
    │   ├── tests/
    │   │   └── RegistrationTest.java
    │   │
    │   └── utils/
    │       ├── Config.java
    │       └── ScreenshotListener.java
    │
    └── resources/
        └── config/
            ├── dev.properties
            └── prod.properties
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

**ScreenshotListener.java**

* Automatically captures screenshots when a test fails.
* Saves screenshots under `target/screenshots/`.

---

# Test Scenarios

The following scenarios are implemented and verified:

| # | Test Scenario      | Validation                                              |
| - | ------------------ | ------------------------------------------------------- |
| 1 | Valid Registration | Valid data can proceed to the email-verification screen |
| 2 | Empty Form         | Register button remains disabled                        |
| 3 | Invalid Email      | Angular `is-invalid` state is validated                 |
| 4 | Password Mismatch  | Register button remains disabled                        |
| 5 | Weak Password      | Angular `is-invalid` state is validated                 |
| 6 | Terms Not Accepted | Register button remains disabled                        |

### Test Execution Result

The dev suite was executed using:

```bash
mvn clean test -Denv=dev
```

Result:

```text
6 tests run
0 failures
0 errors
0 skipped
```

---

# Page Object Model

The framework follows the Page Object Model design pattern.

All page-specific locators and actions are maintained in:

```text
RegistrationPage.java
```

For example:

```java
private By emailInput = By.cssSelector("...");
private By passwordInput = By.cssSelector("...");
private By registerButton = By.cssSelector("...");
```

Test classes interact with the page through reusable methods such as:

```java
enterEmail()
enterPassword()
acceptTerms()
clickRegister()
getValidationMessages()
```

This approach helps:

* Reduce duplicate Selenium code.
* Improve maintainability.
* Keep test cases readable.
* Make locator changes easier to manage.

---

# Synchronization

The framework uses **explicit waits with `WebDriverWait`**.

Examples include waiting for:

* Elements to become visible.
* Elements to become clickable.
* Dynamic validation states.
* Registration-page elements to become available.

The framework intentionally does **not** use:

```java
Thread.sleep()
```

or implicit waits.

This improves test stability and avoids unnecessary fixed delays.

---

# Test Data

The framework generates a **unique email address** for registration tests.

This helps prevent failures caused by attempting to register an email address that has already been used in a previous execution.

---

# Configuration

The default environment is `dev`.

The development URL is maintained in:

```text
src/test/resources/config/dev.properties
```

Production configuration is maintained in:

```text
src/test/resources/config/prod.properties
```

### Run Development Environment

```bash
mvn clean test -Denv=dev
```

### Run Production Environment

```bash
mvn clean test -Denv=prod
```

### Override URL

The framework supports URL overrides without changing the tracked configuration files.

Example:

```bash
mvn clean test -Dregistration.url=https://your-registration-url.com
```

Environment-variable configuration is also supported.

Example in PowerShell:

```powershell
$env:TEST_ENV = "prod"
$env:REGISTRATION_URL_PROD = "https://your-production-registration-page.example"
mvn clean test
```

Configuration precedence:

```text
-Dregistration.url
        ↓
REGISTRATION_URL_<ENV>
        ↓
config/<env>.properties
```

---

# Headless Execution

The tests can also be executed in headless Chrome mode:

```bash
mvn clean test -Denv=dev -Dheadless=true
```

This is useful for CI/CD execution.

---

# Reports and Test Evidence

After execution, reports are available under:

```text
target/surefire-reports/
```

Failure screenshots are automatically stored under:

```text
target/screenshots/
```

Screenshots are captured by the TestNG `ScreenshotListener` when a test fails.

---

# AUT Scope and Locator Strategy

The automation was implemented against the **Registration AUT supplied with the assignment**.

The available registration page includes fields/selectors for:

* First Name
* Last Name
* Email
* Country of Nationality
* Password
* Confirm Password
* Terms/Agreement checkbox
* Register button
* Email verification screen
* Success/notification messages

The framework uses the selectors available in the supplied AUT rather than creating assumptions about elements that are not present.

Validation messages are collected using the available Angular validation states and common validation-message patterns.

For example, invalid email validation is verified using the Angular:

```text
is-invalid
```

state.

The production environment also exposes a toast message such as:

```text
"email" must be a valid email
```

---

# Assignment Scope Clarification

The assignment sheet mentions additional fields such as:

* Organization Name
* Phone Number
* Gender
* State
* Sign Up Text Message checkbox

These fields were **not available in the supplied Registration AUT / available selectors** used for this implementation.

Therefore, no unreliable or fabricated locators were added for these fields.

Similarly, the available registration flow proceeds to an **email-verification screen**. The automation validates that the verification screen is reached and that the registered email is displayed, rather than attempting to automate an email OTP/code that is outside the supplied UI flow.

For the phone-number validation scenario specifically, no phone-number field was available in the supplied AUT, so this scenario was not implemented against a non-existent element.

This keeps the automation aligned with the actual application under test.

---

# Design and Stability Practices

The framework follows these practices:

* Page Object Model for maintainability.
* Explicit waits instead of `Thread.sleep()`.
* No implicit waits.
* Fresh browser session for each test.
* Reusable page-level methods.
* Unique test data to avoid registration conflicts.
* TestNG assertions for validation.
* Automatic screenshots on failure.
* Environment-based URL configuration.
* Maven-based execution.
* TestNG/Surefire reporting.
* Git-based version control.

---

# How to Execute

### 1. Clone the repository

```bash
git clone https://github.com/vishwajeet14all/Selenium-Tests.git
```

### 2. Navigate to the project

```bash
cd Selenium-Tests
```

### 3. Run the test suite

```bash
mvn clean test -Denv=dev
```

### 4. View the test results

```text
target/surefire-reports/
```

### 5. View failure screenshots

```text
target/screenshots/
```

---

# Final Result

The automation framework successfully executes the available registration scenarios against the supplied AUT.

**Latest verified execution:**

```text
Environment: DEV
Tests: 6
Passed: 6
Failed: 0
Errors: 0
Skipped: 0
```

The project demonstrates Selenium UI automation using Java, TestNG, Maven, explicit waits, Page Object Model, configurable environments, assertions, reporting, and failure screenshots.
