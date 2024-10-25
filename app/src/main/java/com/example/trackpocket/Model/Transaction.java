package com.example.trackpocket.Model;

public class Transaction {
    private double amount;
    private String id;
    private String description;
    private String date;
    private String type;

    public Transaction(double amount,String description, String id, String date, String type) {
        this.amount = amount;
        this.id = id;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    public Transaction(){

    }

    public double getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }
}
