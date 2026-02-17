package tests.model.login;

/**
 * One dataset row for login regression execution.
 */
public class LoginCaseData {
    private final String tcId;
    private final String scenarioKey;
    private final String description;
    private final String username;
    private final String password;
    private final String expectedResult;
    private final String expectedErrorText;
    private final boolean runSmoke;
    private final boolean runRegression;
    private final boolean enabled;
    private final int priority;

    public LoginCaseData(
            String tcId,
            String scenarioKey,
            String description,
            String username,
            String password,
            String expectedResult,
            String expectedErrorText,
            boolean runSmoke,
            boolean runRegression,
            boolean enabled,
            int priority) {
        this.tcId = tcId;
        this.scenarioKey = scenarioKey;
        this.description = description;
        this.username = username;
        this.password = password;
        this.expectedResult = expectedResult;
        this.expectedErrorText = expectedErrorText;
        this.runSmoke = runSmoke;
        this.runRegression = runRegression;
        this.enabled = enabled;
        this.priority = priority;
    }

    public String getTcId() {
        return tcId;
    }

    public String getScenarioKey() {
        return scenarioKey;
    }

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getExpectedErrorText() {
        return expectedErrorText;
    }

    public boolean isRunSmoke() {
        return runSmoke;
    }

    public boolean isRunRegression() {
        return runRegression;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPriority() {
        return priority;
    }
}
