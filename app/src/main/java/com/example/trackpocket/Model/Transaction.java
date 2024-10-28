package com.example.trackpocket.Model;

public class Transaction {
    private double amount;
    private String id;
    private String description;
    private String date;
    private String type;
    private String accountId;
    private String accountTitle;

    public Transaction(double amount,String description, String id, String date, String type, String accountId, String accountTitle) {
        this.amount = amount;
        this.id = id;
        this.description = description;
        this.date = date;
        this.type = type;
        this.accountId = accountId;
        this.accountTitle = accountTitle;
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
    public String getAccountId() {
        return accountId;
    }
    public String getAccountTitle() {
        return accountTitle;
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
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }
}
