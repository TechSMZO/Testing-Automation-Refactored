package tests.model.order;

/**
 * One dataset row for add-order regression execution.
 */
public class AddOrderCaseData {
    private final String scenarioId;
    private final String buyerName;
    private final String buyerPhone;
    private final String buyerAlternatePhone;
    private final String buyerEmail;
    private final String buyerGst;
    private final String addressLine1;
    private final String addressLine2;
    private final String pincode;
    private final String orderDate;
    private final String orderType;
    private final String productName;
    private final String quantity;
    private final String unitPrice;
    private final String paymentType;
    private final String codMode;
    private final String codAmount;
    private final String shippingCharges;
    private final String totalWeight;
    private final String length;
    private final String width;
    private final String height;
    private final String resellerName;
    private final String resellerGst;
    private final String ewayBill;
    private final String warehouseTitle;
    private final String warehousePhone;
    private final String warehouseEmail;
    private final boolean runEnabled;
    private final int datasetIndex;

    public AddOrderCaseData(
            String scenarioId,
            String buyerName,
            String buyerPhone,
            String buyerAlternatePhone,
            String buyerEmail,
            String buyerGst,
            String addressLine1,
            String addressLine2,
            String pincode,
            String orderDate,
            String orderType,
            String productName,
            String quantity,
            String unitPrice,
            String paymentType,
            String codMode,
            String codAmount,
            String shippingCharges,
            String totalWeight,
            String length,
            String width,
            String height,
            String resellerName,
            String resellerGst,
            String ewayBill,
            String warehouseTitle,
            String warehousePhone,
            String warehouseEmail,
            boolean runEnabled,
            int datasetIndex) {
        this.scenarioId = scenarioId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.buyerAlternatePhone = buyerAlternatePhone;
        this.buyerEmail = buyerEmail;
        this.buyerGst = buyerGst;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.pincode = pincode;
        this.orderDate = orderDate;
        this.orderType = orderType;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.paymentType = paymentType;
        this.codMode = codMode;
        this.codAmount = codAmount;
        this.shippingCharges = shippingCharges;
        this.totalWeight = totalWeight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.resellerName = resellerName;
        this.resellerGst = resellerGst;
        this.ewayBill = ewayBill;
        this.warehouseTitle = warehouseTitle;
        this.warehousePhone = warehousePhone;
        this.warehouseEmail = warehouseEmail;
        this.runEnabled = runEnabled;
        this.datasetIndex = datasetIndex;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public String getBuyerAlternatePhone() {
        return buyerAlternatePhone;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public String getBuyerGst() {
        return buyerGst;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getPincode() {
        return pincode;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getProductName() {
        return productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getCodMode() {
        return codMode;
    }

    public String getCodAmount() {
        return codAmount;
    }

    public String getShippingCharges() {
        return shippingCharges;
    }

    public String getTotalWeight() {
        return totalWeight;
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

    public String getResellerName() {
        return resellerName;
    }

    public String getResellerGst() {
        return resellerGst;
    }

    public String getEwayBill() {
        return ewayBill;
    }

    public String getWarehouseTitle() {
        return warehouseTitle;
    }

    public String getWarehousePhone() {
        return warehousePhone;
    }

    public String getWarehouseEmail() {
        return warehouseEmail;
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
