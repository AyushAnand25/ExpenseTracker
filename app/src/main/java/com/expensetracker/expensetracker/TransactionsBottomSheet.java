package com.expensetracker.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TransactionsBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView TransactionsRecycler;
    private TextView Text;
    private ArrayList<Transaction> allTransactions = new ArrayList<>();
    private ArrayList<Transaction> transactionsList = new ArrayList<>();
    private String day = "", category;
    private int TYPE, DAY = 1, CATEGORY = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transactions_bottom_sheet, container, false);
        Text = view.findViewById(R.id.TransactionsText);
        TransactionsRecycler = view.findViewById(R.id.RecyclerView);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM");

        if(TYPE == DAY) {
            Text.setText("Transactions on " + day);
            for (Transaction transaction : allTransactions) {
                if (dayFormat.format(new Date(transaction.time)).equals(day))
                    transactionsList.add(transaction);
            }
        }
        else
        {
            Text.setText("Transactions on " + category);
            for (Transaction transaction : allTransactions) {
                if (transaction.category.equals(category))
                    transactionsList.add(transaction);
            }
        }


        RecyclerAdapter_Transactions recyclerAdapter_transactions = new RecyclerAdapter_Transactions(getContext(), transactionsList, RecyclerAdapter_Transactions.BOTTOMSHEET);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        TransactionsRecycler.setLayoutManager(linearLayoutManager);
        TransactionsRecycler.setAdapter(recyclerAdapter_transactions);


        return view;
    }

    void setDayData(String day, ArrayList<Transaction> transactionsList)
    {
        this.day = day;
        this.allTransactions = transactionsList;
        TYPE = DAY;
    }

    void setCategoryData(String categoty, ArrayList<Transaction> transactionsList)
    {
        this.category = categoty;
        this.allTransactions = transactionsList;
        TYPE = CATEGORY;
    }
}
