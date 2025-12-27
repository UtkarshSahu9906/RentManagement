package com.utkarsh.rentmanagement.model;



public class RentableItem {
    private String itemCode;
    private String itemName;
    private String description;
    private String category;
    private boolean rentCalculateDayWise;
    private int quantity;
    private double rentAmount;
    private long createdAt;
    private long updatedAt;
    private String createdBy;

    // Required empty constructor
    public RentableItem() {}

    // Constructor
    public RentableItem(String itemCode, String itemName, String description,
                        String category, boolean rentCalculateDayWise,
                        int quantity, double rentAmount, String createdBy) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.rentCalculateDayWise = rentCalculateDayWise;
        this.quantity = quantity;
        this.rentAmount = rentAmount;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isRentCalculateDayWise() { return rentCalculateDayWise; }
    public void setRentCalculateDayWise(boolean rentCalculateDayWise) {
        this.rentCalculateDayWise = rentCalculateDayWise;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getRentAmount() { return rentAmount; }
    public void setRentAmount(double rentAmount) { this.rentAmount = rentAmount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper methods
    public String getRentType() {
        return rentCalculateDayWise ? "Daily" : "Hourly";
    }

    public String getFormattedRent() {
        return String.format("₹%.0f/%s", rentAmount, rentCalculateDayWise ? "day" : "hour");
    }

    public boolean isAvailable() {
        return quantity > 0;
    }
}