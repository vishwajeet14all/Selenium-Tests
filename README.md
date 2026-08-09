# Registration Page Automation

Selenium + TestNG automation of the [4Excelerate Registration page](https://dev.4excelerate.net/auth/registration) implemented with Java 17, Maven, TestNG, Chrome, WebDriverManager, explicit waits, and the Page Object Model (POM).

> **Video walkthrough:** https://drive.google.com/file/d/185-jqNC1FGz9nVvBCdTwSLzIwMr_G4WP/view?usp=sharing

---

## Tech Stack

| Concern | Choice |
|---|---|
| Language | Java 17 |
| Automation Tool | Selenium WebDriver 4.x |
| Test Framework | TestNG 7.x |
| Build Tool | Maven |
| Browser | Google Chrome |
| Driver Management | WebDriverManager (auto-downloads matching ChromeDriver) |
| Design Pattern | Page Object Model (POM) |
| Synchronization | Explicit waits only — no `Thread.sleep()`, no implicit waits |
| Version Control | Git / GitHub |

---

## How To Run

The suite defaults to the **dev** profile.

```bash
# Dev (default)
mvn clean test -Denv=dev

# Production profile
mvn clean test -Denv=prod

# Headless (useful on CI / build agents)
mvn clean test -Denv=dev -Dheadless=true
```

### Environment Overrides

Any value in `src/test/resources/config/<env>.properties` can be overridden **without editing tracked files**. Precedence is highest → lowest:

1. JVM system property — `-Dregistration.url=https://your-url`
2. Environment variable — `REGISTRATION_URL_DEV`, `REGISTRATION_URL_PROD`, etc.
3. Value in the matching `config/<env>.properties` file

Example on PowerShell:

```powershell
$env:TEST_ENV = "prod"
$env:REGISTRATION_URL_PROD = "https://your-production-registration-page.example"
mvn clean test
```

If no URL is configured the suite skips with a clear message rather than failing on the wrong page.

---

## Reports & Evidence

| Artifact | Location |
|---|---|
| TestNG HTML report | `target/surefire-reports/index.html` |
| Surefire HTML report | `target/surefire-reports/html/` |
| TestNG XML results | `target/surefire-reports/testng-results.xml` |
| Failure screenshots | `target/screenshots/<testMethod>_<epochMillis>.png` (auto-captured by `ScreenshotListener`) |
| CI artifacts (GitHub Actions) | Downloadable from the workflow run page — `surefire-reports` and `failure-screenshots` |

### Continuous Integration

A GitHub Actions workflow at `.github/workflows/test.yml` runs the full suite against the
**dev** profile in headless mode on every push and pull request to `main`. The workflow
uploads the TestNG HTML report and any failure screenshots as build artifacts so the
cause of a red build is one click away from the Actions tab.

```yaml
# Triggered on: push to main, pull_request to main
# Runs: mvn clean test -Denv=dev -Dheadless=true
# Artifacts: surefire-reports/, failure-screenshots/
```

---

## Project Structure

```
Selenium/
├── pom.xml                          Maven build + dependency declarations
├── testng.xml                       TestNG suite config (listeners, classes)
├── README.md                        You are here
└── src/test/
    ├── java/com/automation/
    │   ├── base/BaseTest.java         WebDriver lifecycle (setup + teardown)
    │   ├── pages/RegistrationPage.java  Locators + actions for the AUT
    │   ├── tests/RegistrationTest.java   @Test scenarios + assertions
    │   └── utils/
    │       ├── Config.java             Profile loading + override resolution
    │       └── ScreenshotListener.java  Auto-screenshot on test failure
    └── resources/config/
        ├── dev.properties             Dev profile (registration URL, etc.)
        └── prod.properties            Prod profile
```

### Responsibility Map

| File | Responsibility |
|---|---|
| `BaseTest` | Initializes ChromeDriver, configures options/timeouts, navigates to the registration URL, and tears down the session after every test. |
| `RegistrationPage` | Holds all CSS selectors and exposes high-level user actions (enter name, select country, accept terms, submit). Validations are queried through helper methods. |
| `RegistrationTest` | Owns the `@Test` scenarios, generates unique test data, and asserts expected outcomes. Calls only Page Object methods — never touches locators. |
| `Config` | Resolves which profile (`dev`/`prod`) to load and reads values with system-property → env-var → properties-file precedence. |
| `ScreenshotListener` | TestNG `ITestListener` that captures a PNG of the browser the moment any test fails. |
| `NegativeRegistrationTest` | Data-driven `@DataProvider`-backed scenarios that extend negative-email and weak-password coverage beyond the single example in `RegistrationTest`. |

---

## Test Coverage

### Assignment Requirements — Status

The assignment sheet specifies **6 mandatory test cases** and **11 form fields**. Coverage against that sheet:

| # | Assignment Requirement | Status | Notes |
|---|---|---|---|
| **Test cases** | | | |
| 1 | Valid registration | ✅ Implemented (`validRegistration`) | Asserts verification screen shows the registered email. |
| 2 | Empty form submission | ✅ Implemented (`emptyFormSubmissionShowsValidation`) | Asserts Register button is disabled. |
| 3 | Invalid email format | ✅ Implemented (`invalidEmailShowsValidation`) | Asserts `is-invalid` CSS class on email input. |
| 4 | Password mismatch | ✅ Implemented (`passwordMismatchShowsValidation`) | Asserts Register button stays disabled. |
| 5 | Weak password | ✅ Implemented (`weakPasswordShowsValidation`) | Asserts `is-invalid` class on password input. |
| 6 | Terms & Conditions not checked | ✅ Implemented (`termsNotAcceptedShowsValidation`) | Asserts Register button stays disabled. |
| – | Phone number < 10 digits | ⚠️ Not implemented | See "Gaps & Investigation" below. |
| **Fields** | | | |
| 1 | Name (First / Last) | ✅ | Inputs keyed by placeholder. |
| 2 | Email | ✅ | `<input type="email">`. |
| 3 | Org Name | ⚠️ Not in current AUT | See "Gaps & Investigation" below. |
| 4 | Password | ✅ | `<input placeholder='Password'>`. |
| 5 | Terms & Conditions (Checkbox) | ✅ | Toggled via JS click for reliability. |
| 6 | Continue button | ⚠️ Not in current AUT | AUT submits directly via Register. |
| 7 | Country | ✅ | Autocomplete dropdown, selected by displayed text. |
| 8 | Phone Number | ⚠️ Not in current AUT | See "Gaps & Investigation" below. |
| 9 | State | ⚠️ Not in current AUT | See "Gaps & Investigation" below. |
| 10 | Sign Up Text (Checkbox) | ⚠️ Not in current AUT | The Terms checkbox appears to cover the only required checkbox. |
| 11 | Register Button | ✅ | `app-button > .form-group > :nth-child(1)`. |

### Gaps & Investigation Notes

The live AUT at `https://dev.4excelerate.net/auth/registration` exposes only the following inputs at the time of this submission:

- First Name, Last Name, Email, Country of Nationality, Password, Confirm Password, Terms & Conditions checkbox, Register button.

The following assignment items could not be located on the current AUT despite inspecting the DOM, the network responses, with the task:

- **Org Name** — no input with placeholder/label matching "Organization", "Org", "Company", or similar was found.
- **Phone Number** — no `tel` input or `[placeholder*='Phone']` selector present. Without a phone field, the **phone number < 10 digits** test case also cannot be implemented.
- **State** — no state input or dropdown found; the country selector does not surface dependent state options.
- **Continue button** — the AUT has a single Register button that submits the form. There is no intermediate Continue step in the current build.
- **Sign Up Text checkbox** — only the Terms & Conditions checkbox is present. If this refers to an email-marketing/newsletter opt-in, it is not exposed on the current page.

A `phoneNumberLessThanTenDigitsShowsValidation` test and `selectGender` method were intentionally not added because the underlying fields do not exist on the AUT — adding them would have produced false-positive passes against stale or fabricated locators. If the AUT is updated to expose these fields, the page object and tests can be extended directly.

---

## Verified Result

Dev suite execution:

```bash
mvn clean test -Denv=dev
```

```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

---

## Design Notes

- **Per-test isolation** — each test method receives a fresh Chrome session from `BaseTest`, so state from one test never leaks into another.
- **Synchronization** — `WebDriverWait` (10s default) is used for every interaction and validation read. No `Thread.sleep()` and no implicit wait anywhere in the codebase.
- **Tab to blur** — every `type()` helper sends `Keys.TAB` after input so Angular's form validators run before the test reads the resulting state.
- **Unique test data** — every run generates a fresh email (`qa.<epochMs>@example.com`) so the suite can be replayed against the same environment without collisions.
- **Failure evidence** — `ScreenshotListener` (registered globally in `testng.xml`) saves a PNG of the browser to `target/screenshots/` whenever any test fails, so triage never depends on logs alone.
- **Override-friendly config** — the precedence order (`-D` > env var > properties file) lets CI/CD inject URLs without modifying tracked files.

---

## Prerequisites

- JDK 17 or newer
- Maven 3.8 or newer
- Google Chrome
- Network access on the first run so WebDriverManager can resolve ChromeDriver

---

## Environment Configuration

| Profile | URL | Notes |
|---|---|---|
| `dev`  | `https://dev.4excelerate.net/auth/registration` | Default profile, used by `mvn clean test` when `-Denv` is not set. |
| `prod` | `https://experience.4excelerate.org/auth/registration` | Production registration route. Select with `-Denv=prod` or `TEST_ENV=prod`. |

Both values can still be overridden at runtime via `-Dregistration.url=...` or `REGISTRATION_URL_DEV` / `REGISTRATION_URL_PROD` without editing tracked files.


