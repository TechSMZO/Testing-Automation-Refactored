package tests.model.registration;

/**
 * One dataset row for registration regression execution.
 */
public class RegistrationCaseData {
    private final String scenarioId;
    private final String userType;
    private final String name;
    private final String email;
    private final String phone;
    private final String password;
    private final String confirmPassword;
    private final boolean agreeTerms;
    private final int datasetIndex;

    public RegistrationCaseData(
            String scenarioId,
            String userType,
            String name,
            String email,
            String phone,
            String password,
            String confirmPassword,
            boolean agreeTerms,
            int datasetIndex) {
        this.scenarioId = scenarioId;
        this.userType = userType;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.agreeTerms = agreeTerms;
        this.datasetIndex = datasetIndex;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getUserType() {
        return userType;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public boolean isAgreeTerms() {
        return agreeTerms;
    }

    public int getDatasetIndex() {
        return datasetIndex;
    }

    public String getDatasetId() {
        return scenarioId.toUpperCase() + "_" + String.format("%02d", datasetIndex);
    }
}
