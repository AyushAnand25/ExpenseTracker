package com.expensetracker.expensetracker;

import android.util.Log;

public class Transaction {

    String id, sms, category, details, account_no, payment_method;
    int type;
    Long time;
    Double amount;
    boolean ignore;
    static int DEBIT = 1, CREDIT = 2;

    public Transaction(String id, String sms, int type, String category, String details, String account_no, String payment_method,
                       Long time, Double amount, boolean ignore) {
        this.id = id;
        this.sms = sms;
        this.type = type;
        this.category = category;
        this.details = details;
        this.account_no = account_no;
        this.payment_method = payment_method;
        this.time = time;
        this.amount = amount;
        this.ignore = ignore;
    }
}
