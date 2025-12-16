package com.utkarsh.rentmanagement.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ownership {
    private List<Shop> shops;
    private Date createdAt;

    public Ownership() {
        this.shops = new ArrayList<>();
        this.createdAt = new Date();
    }

    public Ownership(List<Shop> shops) {
        this.shops = shops;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public List<Shop> getShops() { return shops; }
    public void setShops(List<Shop> shops) { this.shops = shops; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}