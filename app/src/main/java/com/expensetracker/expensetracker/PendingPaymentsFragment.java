package com.expensetracker.expensetracker;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PendingPaymentsFragment extends Fragment implements RecyclerAdapter_PendingPayments.PendingListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView TransactionsRecycler;
    private ProgressBar Loading;
    private TextView ToolbarName;
    private Button Back;
    private TextView Pending, Debit, Credit;
    private Button New;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ArrayList<Transaction> transactionsList = new ArrayList<>();
    RecyclerAdapter_PendingPayments recyclerAdapter_pendingPayments;
    private double debit = 0.0, credit = 0.0, settle = 0.0;
    private HashSet<Integer> selectedPayments = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pending_payments, container, false);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        Loading = view.findViewById(R.id.Loading);
        Back = view.findViewById(R.id.Back);
        swipeRefreshLayout= view.findViewById(R.id.SwipeRefreshLayout);
        TransactionsRecycler = view.findViewById(R.id.RecyclerView);
        Pending = view.findViewById(R.id.PendingAmount);
        Debit = view.findViewById(R.id.DebitAmount);
        Credit = view.findViewById(R.id.CreditAmount);
        New = view.findViewById(R.id.Add);
        ToolbarName.setText("Pending Transactions");
        UtilityFunctions.animateButton(getContext(), TransactionsRecycler, New, true);

        New.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddExpenseFragment addExpenseFragment = new AddExpenseFragment();
                addExpenseFragment.setData(null, true, PendingPaymentsFragment.this);
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.Frame_Container, addExpenseFragment, "ADD").addToBackStack(null).commit();
            }
        });


        //Back
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                getTransactions();
            }
        });


        getTransactions();
        return view;
    }


    void getTransactions() {

        transactionsList.clear();
        debit = credit = settle = 0.0;
        firestore.collection("PendingPayments").document(MainActivity.userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                HashMap<String, Object> transactions = (HashMap<String, Object>) documentSnapshot.get("PendingPayments");

                if (transactions != null) {
                    for (String key : transactions.keySet()) {
                        HashMap<String, Object> transaction = (HashMap<String, Object>) transactions.get(key);
                        String type = (String) transaction.get("Type");
                        int transaction_type = Transaction.DEBIT;
                        Double amount;
                        Boolean ignore = false;

                        try {
                            amount = (Double) transaction.get("Amount");
                        } catch (Exception e) {
                            amount = ((Long) transaction.get("Amount")).doubleValue();
                        }

                        if (type.equals("Debit")) {
                            debit += amount;
                        } else {
                            credit += amount;
                            transaction_type = Transaction.CREDIT;
                        }

                        if (transaction.containsKey("Ignore"))
                            ignore = (Boolean) transaction.get("Ignore");

                        transactionsList.add(new Transaction((String) transaction.get("Id"), (String) transaction.get("SMS"),
                                transaction_type, (String) transaction.get("Category"), (String) transaction.get("Details"), (String) transaction.get("AccountNo"),
                                (String) transaction.get("PaymentMethod"), (Long) transaction.get("Time"), amount, ignore));
                    }
                }

                Loading.setVisibility(View.GONE);
                recyclerAdapter_pendingPayments = new RecyclerAdapter_PendingPayments(getContext(), transactionsList, selectedPayments, credit, debit, PendingPaymentsFragment.this);
                final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                TransactionsRecycler.setLayoutManager(linearLayoutManager);
                TransactionsRecycler.setAdapter(recyclerAdapter_pendingPayments);
            }
        });

    }


    @Override
    public void onLongPress(int index) {

        if(!recyclerAdapter_pendingPayments.selectionEnabled) {
            selectedPayments.add(index);
            Double amount = transactionsList.get(index).amount;
            if(transactionsList.get(index).type == Transaction.DEBIT)
                amount = -amount;

            settle += amount;
            New.setText(String.format("Settle ₹%.1f", settle));
        }
        else {
            selectedPayments.clear();
            New.setText("New");
        }

        recyclerAdapter_pendingPayments.selectionEnabled = !recyclerAdapter_pendingPayments.selectionEnabled;
        recyclerAdapter_pendingPayments.notifyDataSetChanged();
    }


    @Override
    public void onClick(int index) {

        if(recyclerAdapter_pendingPayments.selectionEnabled) {

            Double amount = transactionsList.get(index).amount;
            if(transactionsList.get(index).type == Transaction.DEBIT)
                amount = -amount;

            if (selectedPayments.contains(index)) {
                selectedPayments.remove(index);
                amount = -amount;
            }
            else
                selectedPayments.add(index);

            settle += amount;
            New.setText(String.format("Settle ₹%.1f", settle));
            if(selectedPayments.isEmpty())
            {
                recyclerAdapter_pendingPayments.selectionEnabled = false;
                New.setText("New");
            }
            recyclerAdapter_pendingPayments.notifyDataSetChanged();
        }
    }

    @Override
    public void onEdit(int index){
        AddExpenseFragment addExpenseFragment = new AddExpenseFragment();
        addExpenseFragment.setData(transactionsList.get(index), true, PendingPaymentsFragment.this);
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.Frame_Container, addExpenseFragment, "ADD").addToBackStack(null).commit();
    }

    @Override
    public void onDelete(int index) {

        Dialog dialog = new Dialog(getContext());
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Button Delete, Cancel;
        TextView Message = dialog.findViewById(R.id.Message);
        Delete = dialog.findViewById(R.id.Ok);
        Cancel = dialog.findViewById(R.id.Cancel);

        Message.setText("Delete this Pending Payment?");
        Delete.setText("Delete");
        Cancel.setText("Cancel");

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("PendingPayments").document(MainActivity.userId).update("PendingPayments." + transactionsList.get(index).id, FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        transactionsList.remove(index);
                        recyclerAdapter_pendingPayments.notifyItemRemoved(index + 1);
                        recyclerAdapter_pendingPayments.notifyItemRangeChanged(index + 1,transactionsList.size() + 1);
                    }
                });
                dialog.dismiss();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onSettle(int index) {
            Dialog dialog = new Dialog(getContext());
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_layout);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            Button Settle, Cancel;
            TextView Message = dialog.findViewById(R.id.Message);
            Settle = dialog.findViewById(R.id.Ok);
            Cancel = dialog.findViewById(R.id.Cancel);

            Message.setText("Settle this Pending Payment?");
            Settle.setText("Settle");
            Cancel.setText("Cancel");

            Settle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firestore.collection("PendingPayments").document(MainActivity.userId).update("PendingPayments." + transactionsList.get(index).id, FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            TransactionData transactionData = new TransactionData(getContext());
                            transactionData.addTransaction(transactionsList.get(index), false, PendingPaymentsFragment.this, false);

                            transactionsList.remove(index);
                            recyclerAdapter_pendingPayments.notifyItemRemoved(index + 1);
                            recyclerAdapter_pendingPayments.notifyItemRangeChanged(index + 1,transactionsList.size() + 1);


                        }
                    });
                    dialog.dismiss();
                }
            });

            Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
    }
}
