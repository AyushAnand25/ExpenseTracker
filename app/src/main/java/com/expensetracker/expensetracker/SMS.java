package com.expensetracker.expensetracker;

public class SMS {

    String sms_id, category, payment_method, bank, address, body, account_no, details;
    Long date;
    int type;
    Double amount, balance;
    boolean read;

    SMS(String id, int type, String category, String payment_method, String bank, String address, String message, String account_no, Long date, Double amount, Double balance, boolean read, String details) {
        this.sms_id = id;
        this.type = type;
        this.category = category;
        this.payment_method = payment_method;
        this.bank = bank;
        this.address = address;
        this.body = message;
        this.account_no = account_no;
        this.date = date;
        this.amount = amount;
        this.balance = balance;
        this.read = read;
        this.details = details;
    }
}
