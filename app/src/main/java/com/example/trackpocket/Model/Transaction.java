package com.example.trackpocket.Model;

public class Transaction {
    private double amount;
    private String category;
    private String id;
    private String note;
    private String date;
    private String type;

    public Transaction(double amount, String category, String id, String note, String date, String type) {
        this.amount = amount;
        this.category = category;
        this.id = id;
        this.note = note;
        this.date = date;
        this.type = type;
    }

    public Transaction(){

    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getNote() {
        return note;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setType(String type) {
        this.type = type;
    }
}
