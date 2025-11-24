package com.utkarsh.rentmanagement.model;

public class Item {
    private String id;
    private String title;
    private String description;
    private double price;
    private String category;

    // Constructors
    public Item() {
        // Default constructor required for Firebase
    }

    public Item(String id, String title, String description, double price, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}