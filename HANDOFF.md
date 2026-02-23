# Selenium UI Automation - Context Handoff

## Project Baseline
- Stack: Java 17, Selenium WebDriver, TestNG, Maven, ExtentReports.
- Main refactor scope in this phase: `src/test/java/tests/**` (simple legacy test package).
- Goal: keep code beginner-friendly, avoid over-abstraction, keep behavior stable.

## Team Coding Rules (Active)
- Create a new function only when logic is reused or likely to be reused.
- Keep one-time logic directly inside testcase methods.
- Avoid tiny pass-through wrappers and extra layers.
- Prefer linear, easy-to-read flow for fresh QA engineers.
- Add short comments only where logic is not obvious.
- After changes, always explain:
  - what changed
  - why changed
  - why this approach (and not heavier alternatives)
  - validation performed

## Architecture Decisions Currently Applied
- Both data styles are retained:
  - hardcoded DataProviders
  - CSV-driven DataProviders
- Login regression structure:
  - one shared function for common login actions
  - scenario-specific assertions kept directly in each test method
- Config lookup pattern stays simple:
  - env var -> system property -> fallback

## Login Area - Current State

### `src/test/java/tests/LoginRegressionTest.java`
- Three test methods:
  - `login_valid_login_should_open_dashboard`
  - `login_invalid_password_should_show_error`
  - `login_unregistered_phone_should_show_error`
- Shared function:
  - `loginWithData(LoginCaseData data)` only for common login steps
- Unique assertions are inline per testcase (readability-first).
- Over-abstracted single-use helpers were removed.

### `src/test/java/tests/data/login/LoginDataProvider.java`
- `loginData(Method method)`:
  - hardcoded rows
  - filtered by `@Test(description = "scenario_id")`
  - each testcase gets its own scenario rows only
- `loginDataCsv(Method method)`:
  - CSV-based rows
  - filtered by same scenario mapping pattern
- Both providers are kept intentionally.

### `src/test/java/tests/pages/LoginPage.java`
- Explicit actions/checks only:
  - `open`, `waitUntilLoaded`, `login`, `waitForDashboard`
  - `isDashboardLoaded`, `isInvalidCredentialsVisible`, `isUnregisteredPhoneVisible`
- Generic page-source text parsing approach was removed in favor of explicit element waits.

### `src/test/java/tests/LoginTest.java`
- Fixed compile/runtime mismatch:
  - no longer calls removed `waitForLoginOutcome(...)`
  - now uses `login(...)` + `waitForDashboard(...)`
- Current smoke login flow is passing.

## Registration Area - Current State

### `src/test/java/tests/data/registration/RegistrationDataProvider.java`
- Both hardcoded + CSV providers exist.
- Removed unnecessary pass-through wrapper (`getDataRows` style).

### `src/test/java/tests/RegistrationTest.java`
- Simplified style and comments improved.
- Still has runtime failure in current environment (see known issue below).

### `src/test/java/tests/pages/RegistrationPage.java`
- Direct form actions and outcome checks.
- Uses dropdown selection for user type currently by option text.

## Utilities Cleanup Done
- `src/test/java/tests/utils/BaseTest.java`
  - added concise method-level comments
- `src/test/java/tests/utils/CrossBrowserFactory.java`
  - removed placeholder noise comments
  - cleaned formatting
- `src/test/java/tests/utils/TestSuiteRunner.java`
  - removed placeholder noise comments
  - clarified method intent with brief comments

## Known Open Issue
- Registration runtime failure:
  - timeout while selecting dropdown option `"Seller"`
  - failing location: `src/test/java/tests/pages/RegistrationPage.java` in `selectUserType(...)`
  - typical error: unable to click `//li[normalize-space()='Seller']` within timeout
- This is a locator/timing robustness issue, not a compile issue.

## Validation History (Most Recent)
- `mvn -q -DskipTests=true test-compile` -> PASS
- `mvn -q test "-Dtest=tests.LoginTest"` -> PASS
- `mvn -q test "-Dtest=tests.LoginRegressionTest"` -> PASS
- `mvn -q test "-Dtest=tests.RegistrationTest"` -> FAIL (dropdown option click timeout)

## What To Do Next (Priority)
1. Make registration dropdown selection robust (open listbox, wait for visible options, then click exact item).
2. Re-run `RegistrationTest` after dropdown fix.
3. Keep the same coding rules:
   - shared steps extracted only when reused
   - testcase-specific assertions inline
   - no extra abstraction layers

## Important Files For Fast Continuation
- `src/test/java/tests/LoginRegressionTest.java`
- `src/test/java/tests/LoginTest.java`
- `src/test/java/tests/data/login/LoginDataProvider.java`
- `src/test/java/tests/pages/LoginPage.java`
- `src/test/java/tests/RegistrationTest.java`
- `src/test/java/tests/data/registration/RegistrationDataProvider.java`
- `src/test/java/tests/pages/RegistrationPage.java`
- `src/test/java/tests/utils/BaseTest.java`
- `src/test/resources/testdata/login/login-regression.csv`
- `src/test/resources/testdata/registration/registration-regression.csv`

## Recommended Starter Prompt For Next Thread
Continue from `HANDOFF.md`.
Keep beginner-first style, no over-abstraction.
Fix registration dropdown selection timeout.
Keep both hardcoded and CSV providers.
Explain every change with what/why/why-this-approach and validation.

