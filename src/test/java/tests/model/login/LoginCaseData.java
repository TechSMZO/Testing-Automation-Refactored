package tests.model.login;

/**
 * One dataset row for login regression execution.
 */
public class LoginCaseData {
    private final String scenarioId;
    private final String username;
    private final String password;
    private final int datasetIndex;

    public LoginCaseData(String scenarioId, String username, String password, int datasetIndex) {
        this.scenarioId = scenarioId;
        this.username = username;
        this.password = password;
        this.datasetIndex = datasetIndex;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public int getDatasetIndex() {
        return datasetIndex;
    }

    public String getDatasetId() {
        return scenarioId.toUpperCase() + "_" + String.format("%02d", datasetIndex);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
