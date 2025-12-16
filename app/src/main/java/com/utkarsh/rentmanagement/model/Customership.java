package com.utkarsh.rentmanagement.model;

public class Customership {

    private String shopName;
    private String bgImg;
    private String logo;
    private String shopId;


    public Customership(String shopName, String bgImg, String logo, String shopId) {
        this.shopName = shopName;
        this.bgImg = bgImg;
        this.logo = logo;
        this.shopId = shopId;
    }

    public Customership() {
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getBgImg() {
        return bgImg;
    }

    public void setBgImg(String bgImg) {
        this.bgImg = bgImg;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
}
