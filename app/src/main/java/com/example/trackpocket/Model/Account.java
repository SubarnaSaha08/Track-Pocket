package com.example.expensemanager.Model;

public class Account {
    private String title;
    private double balance;

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

