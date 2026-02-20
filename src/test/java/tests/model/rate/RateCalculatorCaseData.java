package tests.model.rate;

/**
 * One dataset row for rate-calculator regression execution.
 */
public class RateCalculatorCaseData {
    private final String scenarioId;
    private final String calculatorMode;
    private final String packageType;
    private final String originPincode;
    private final String destinationPincode;
    private final String weight;
    private final String invoiceValue;
    private final String length;
    private final String width;
    private final String height;
    private final boolean runEnabled;
    private final int datasetIndex;

    public RateCalculatorCaseData(
            String scenarioId,
            String calculatorMode,
            String packageType,
            String originPincode,
            String destinationPincode,
            String weight,
            String invoiceValue,
            String length,
            String width,
            String height,
            boolean runEnabled,
            int datasetIndex) {
        this.scenarioId = scenarioId;
        this.calculatorMode = calculatorMode;
        this.packageType = packageType;
        this.originPincode = originPincode;
        this.destinationPincode = destinationPincode;
        this.weight = weight;
        this.invoiceValue = invoiceValue;
        this.length = length;
        this.width = width;
        this.height = height;
        this.runEnabled = runEnabled;
        this.datasetIndex = datasetIndex;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getCalculatorMode() {
        return calculatorMode;
    }

    public String getPackageType() {
        return packageType;
    }

    public String getOriginPincode() {
        return originPincode;
    }

    public String getDestinationPincode() {
        return destinationPincode;
    }

    public String getWeight() {
        return weight;
    }

    public String getInvoiceValue() {
        return invoiceValue;
    }

    public String getLength() {
        return length;
    }

    public String getWidth() {
        return width;
    }

    public String getHeight() {
        return height;
    }

    public boolean isRunEnabled() {
        return runEnabled;
    }

    public int getDatasetIndex() {
        return datasetIndex;
    }

    public String getDatasetId() {
        return scenarioId.toUpperCase() + "_" + String.format("%02d", datasetIndex);
    }
}
