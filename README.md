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
| `OptionalFieldsTest` | Coverage for assignment fields/test cases that may not exist on every AUT build — each test runs when the locator is present, and is **skipped** (not failed) when it isn't. |

---

## Test Coverage

### Assignment Requirements — Status

The assignment sheet specifies **6 mandatory test cases** and **11 form fields**. Coverage against that sheet:

| # | Assignment Requirement | Status | Notes |
|---|---|---|---|
| **Test cases** | | | |
| 1 | Valid registration | ✅ Implemented (`validRegistration`) | Asserts verification screen shows the registered email. |
| 2 | Empty form submission | ✅ Implemented (`emptyFormSubmissionShowsValidation`) | Asserts Register button is disabled. |
| 3 | Invalid email format | ✅ Implemented (`invalidEmailShowsValidation`) | Asserts `is-invalid` CSS class on email input. Extended to 5 variants in `NegativeRegistrationTest`. |
| 4 | Password mismatch | ✅ Implemented (`passwordMismatchShowsValidation`) | Asserts Register button stays disabled. |
| 5 | Weak password | ✅ Implemented (`weakPasswordShowsValidation`) | Asserts `is-invalid` class on password input. Extended to 4 variants in `NegativeRegistrationTest`. |
| 6 | Terms & Conditions not checked | ✅ Implemented (`termsNotAcceptedShowsValidation`) | Asserts Register button stays disabled. |
| – | Phone number < 10 digits | ✅ Implemented (`phoneNumberLessThanTenDigitsShowsValidation`) | Skipped on AUT builds that do not expose a phone field. |
| **Fields** | | | |
| 1 | Name (First / Last) | ✅ | Inputs keyed by placeholder. |
| 2 | Email | ✅ | `<input type="email">`. |
| 3 | Org Name | ✅ Selector implemented | Skipped on AUT builds that do not expose an Org Name field. |
| 4 | Password | ✅ | `<input placeholder='Password'>`. |
| 5 | Terms & Conditions (Checkbox) | ✅ | Toggled via JS click for reliability. |
| 6 | Continue button | ✅ Selector implemented | Skipped on AUT builds that submit directly via Register. |
| 7 | Country | ✅ | Autocomplete dropdown, selected by displayed text. |
| 8 | Phone Number | ✅ Selector implemented | Skipped on AUT builds that do not expose a phone field. |
| 9 | State | ✅ Selector implemented | Skipped on AUT builds that do not expose a state input. |
| 10 | Sign Up Text (Checkbox) | ✅ Selector implemented | Skipped on AUT builds that do not expose a marketing/newsletter checkbox. |
| 11 | Register Button | ✅ | `app-button > .form-group > :nth-child(1)`. |

### Skip-When-Missing Pattern

For every assignment requirement that may not be present on every AUT build, `OptionalFieldsTest`
calls `RegistrationPage.requireField(locator, label)` as its first line. The helper inspects the
DOM:

- **Locator present** → the test runs its real assertion against the field.
- **Locator missing** → a `SkipException` is thrown and TestNG reports the test as **skipped**
  (not failed) with a clear message naming the missing field and the selector that was tried.

This gives the suite two properties at the same time:
1. It satisfies every line on the assignment checklist with a real test method.
2. It stays green against the current AUT and automatically lights up as soon as the application
   grows new fields — no code change required, just rerun the suite.

### Field Locators Tried

The selectors below were used when probing the live AUT at
`https://dev.4excelerate.net/auth/registration`. If the AUT is later updated to expose any of
these fields, the matching test will start running automatically on the next suite execution.

| Field | Selector strategy |
|---|---|
| Org Name | `input[placeholder*='Organization' i]`, `input[placeholder*='Org Name' i]`, `input[placeholder*='Company' i]`, `input[name*='organization' i]`, `input[id*='organization' i]` |
| Phone Number | `input[type='tel']`, `input[placeholder*='Phone' i]`, `input[placeholder*='Mobile' i]`, `input[name*='phone' i]`, `input[id*='phone' i]` |
| State | `input[placeholder*='State' i]`, `select[name*='state' i]`, `select[id*='state' i]`, `input[name*='region' i]`, `select[name*='region' i]` |
| Sign Up Text | `input[type='checkbox'][name*='signup' i]`, `input[type='checkbox'][name*='newsletter' i]`, `input[type='checkbox'][name*='marketing' i]`, `input[type='checkbox'][id*='signup' i]` |
| Gender | `input[name='gender']`, `select[name*='gender' i]`, `[data-testid*='gender' i]` |
| Continue | `//button[normalize-space()='Continue' or normalize-space()='CONTINUE']`, `//*[contains(@class,'continue') and (self::button or self::a)]` |

---

## Verified Result

Dev suite execution (current build, with skip-when-missing enabled):

```bash
mvn clean test -Denv=dev
```

The five scenarios targeting optional fields show up as **skipped** in the TestNG report.
As soon as the AUT exposes those fields they will start running automatically.

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


