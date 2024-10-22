package com.example.trackpocket.Model;

public class Account {
    private String title;
    private double balance;

    public Account() {
        // This can be left empty or used to initialize default values
    }

    public Account(String title, double balance){
        this.title = title;
        this.balance = balance;
    }

    public String getTitle() {
        return title;
    }

    public double getBalance() {
        return balance;
    }
}

