package com.utkarsh.rentmanagement.model;

public class Customer {
    private String name;
    private String uid;
    private String address;
    private long mobileNo;
    private long aadhaarNo;
    private boolean paid;
    private String imgUrl;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public Customer() {
        // Default constructor required for Firebase
    }

    public Customer(String name, String uid, String address, long mobileNo, long aadhaarNo, boolean paid, String imgUrl, String createdAt, String updatedAt) {
        this.name = name;
        this.uid = uid;
        this.address = address;
        this.mobileNo = mobileNo;
        this.aadhaarNo = aadhaarNo;
        this.paid = paid;
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    // Getters and Setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long  getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(long mobileNo) {
        this.mobileNo = mobileNo;
    }

    public long getAadhaarNo() {
        return aadhaarNo;
    }

    public void setAadhaarNo(long aadhaarNo) {
        this.aadhaarNo = aadhaarNo;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}