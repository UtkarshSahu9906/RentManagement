package com.utkarsh.rentmanagement.model;

public class Shop {
    private String shopId;
    private String shopName;
    private String bgImg;
    private String logo;

    public Shop() {
        // Required for Firestore
    }

    public Shop(String shopId, String shopName, String bgImg, String logo) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.bgImg = bgImg;
        this.logo = logo;
    }

    // Getters and Setters
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getBgImg() { return bgImg; }
    public void setBgImg(String bgImg) { this.bgImg = bgImg; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
}