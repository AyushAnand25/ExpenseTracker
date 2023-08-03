package com.expensetracker.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.expensetracker.expensetracker.Transaction.CREDIT;
import static com.expensetracker.expensetracker.Transaction.DEBIT;

public class AddExpenseFragment extends Fragment implements RecyclerAdapter_Categories.CategoryClick {


    private RecyclerView CategoriesRecycler;
    private ConstraintLayout SMSLayout;
    private TextView ToolbarName;
    private TextView Credit, Debit, Date, SMSText, SMSTime;
    private EditText Amount, Details;
    private Button Add, Back;
    private ArrayList<Category> categoryArrayList = new ArrayList<>();
    private RecyclerAdapter_Categories recyclerAdapter_categories;
    private int current_index = -1;
    private int transaction_type = -1;
    String id = "";
    private Date date;

    private Transaction transaction;
    private Fragment fragment;
    private boolean isPending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        SMSLayout = view.findViewById(R.id.SMSLayout);
        CategoriesRecycler = view.findViewById(R.id.CategoriesRecycler);
        Debit = view.findViewById(R.id.Debit);
        Credit = view.findViewById(R.id.Credit);
        Date = view.findViewById(R.id.Date);
        Add = view.findViewById(R.id.Add);
        Back = view.findViewById(R.id.Back);
        Amount = view.findViewById(R.id.Amount);
        Details = view.findViewById(R.id.Details);
        SMSText = view.findViewById(R.id.SMSText);
        SMSTime = view.findViewById(R.id.Time);
        Amount.requestFocus();
        ToolbarName.setText("Add Transaction");
        MainActivity.BottomNavigationBar.setVisibility(View.GONE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        date = new Date();
        id = UtilityFunctions.getRandomKey();

        for(String key : MainActivity.categoriesMap.keySet())
            categoryArrayList.add(new Category(key, MainActivity.categoriesMap.get(key).category, MainActivity.categoriesMap.get(key).iconUrl, MainActivity.categoriesMap.get(key).type, false));


        //If Edit
        if(transaction != null)
        {
            ToolbarName.setText("Edit Transaction");
            Amount.setText(String.format("₹ %.2f", transaction.amount));
            Amount.setSelection(Amount.getText().length());
            Details.setText(transaction.details);

            if(transaction.sms != null && !transaction.sms.equals("")) {
                SMSLayout.setVisibility(View.VISIBLE);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, dd MMM yy");
                SMSText.setText(transaction.sms);
                SMSTime.setText(simpleDateFormat.format(transaction.time));
            }

            date = new Date(transaction.time);
            id = transaction.id;
            Add.setText("Update");

            if(transaction.type == DEBIT)
            {
                Debit.setBackgroundResource(R.drawable.round_drawable3);
                Debit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                transaction_type = DEBIT;
            }
            else
            {
                Credit.setBackgroundResource(R.drawable.round_drawable3);
                Credit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                SMSTime.setTextColor(ContextCompat.getColor(getContext(),R.color.purple1));
                transaction_type = CREDIT;
            }

            int c = 0;
            for(Category category : categoryArrayList) {
                if (transaction.category.equals(category.category)) {
                    category.isSelected = true;
                    current_index = c;
                }
                c++;
            }
        }


        recyclerAdapter_categories = new RecyclerAdapter_Categories(getContext(), categoryArrayList, this);
        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        CategoriesRecycler.setLayoutManager(linearLayoutManager2);
        CategoriesRecycler.setAdapter(recyclerAdapter_categories);
        Date.setText(dateFormat.format(date));


        Debit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(transaction_type == -1)
                {
                    Debit.setBackgroundResource(R.drawable.round_drawable3);
                    Debit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    transaction_type = DEBIT;
                }
                else if(transaction_type == DEBIT)
                {
                    Debit.setBackgroundResource(R.drawable.border_drawable2);
                    Debit.setTextColor(ContextCompat.getColor(getContext(),R.color.blue3));
                    transaction_type = -1;
                }
                else if(transaction_type == CREDIT)
                {
                    Debit.setBackgroundResource(R.drawable.round_drawable3);
                    Debit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    Credit.setBackgroundResource(R.drawable.border_drawable2);
                    Credit.setTextColor(ContextCompat.getColor(getContext(),R.color.blue3));
                    transaction_type = DEBIT;
                }
            }
        });

        Credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(transaction_type == -1)
                {
                    Credit.setBackgroundResource(R.drawable.round_drawable3);
                    Credit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    transaction_type = CREDIT;
                }
                else if(transaction_type == CREDIT)
                {
                    Credit.setBackgroundResource(R.drawable.border_drawable2);
                    Credit.setTextColor(ContextCompat.getColor(getContext(),R.color.blue3));
                    transaction_type = -1;
                }
                else if(transaction_type == DEBIT)
                {
                    Credit.setBackgroundResource(R.drawable.round_drawable3);
                    Credit.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                    Debit.setBackgroundResource(R.drawable.border_drawable2);
                    Debit.setTextColor(ContextCompat.getColor(getContext(),R.color.blue3));
                    transaction_type = CREDIT;
                }
            }
        });

        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Double amount = Double.parseDouble(Amount.getText().toString().replace("₹ ", "").toString());

                    if(transaction_type == -1)
                        UtilityFunctions.showSnackbar("Please select Transaction Type.");
                    else if(current_index == -1)
                        UtilityFunctions.showSnackbar("Please select the category.");
                    else {
                        Transaction transaction = new Transaction(id, "", transaction_type, categoryArrayList.get(current_index).category, Details.getText().toString(), "", "UPI", date.getTime(), amount.doubleValue(), false);
                        TransactionData transactionData = new TransactionData(getContext());
                        transactionData.addTransaction(transaction, isPending, fragment, true);
                    }
                }catch (Exception e)
                {
                    UtilityFunctions.showSnackbar("Please Enter a valid Amount.");
                }
            }
        });


        //Back
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }


    public void setData(Transaction transaction, boolean isPending, Fragment fragment)
    {
        this.transaction = transaction;
        this.isPending = isPending;
        this.fragment = fragment;
    }

    private void showCalendar() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                //Start Date
                calendar.set(i, i1, i2);
                date = new Date(calendar.getTimeInMillis());
                Date.setText(dateFormat.format(date));
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public void onCategoryClick(int index) {

        if(index == current_index) {
            categoryArrayList.get(index).isSelected = false;
            current_index = -1;
        }
        else
        {
            if(current_index != -1)
                categoryArrayList.get(current_index).isSelected = false;
            categoryArrayList.get(index).isSelected = true;
            current_index = index;
        }
        recyclerAdapter_categories.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {

        MainActivity.BottomNavigationBar.setVisibility(View.VISIBLE);
        super.onDestroy();
    }
}
