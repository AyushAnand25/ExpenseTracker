package com.expensetracker.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class TransactionsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView ToolbarName;
    private ProgressBar Loading;
    private Button Back;
    private TextView MonthText;
    private Spinner MonthSpinner;
    private Button Add;
    private RecyclerView TransactionsRecycler;

    private String current_month = "";
    private ArrayList<String> months = new ArrayList<>(Arrays.asList("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"));
    private ArrayList<String> spinnerData = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        ToolbarName = view.findViewById(R.id.ToolbarName);
        Back = view.findViewById(R.id.Back);
        swipeRefreshLayout= view.findViewById(R.id.SwipeRefreshLayout);
        MonthText = view.findViewById(R.id.MonthText);
        MonthSpinner = view.findViewById(R.id.MonthSpinner);
        TransactionsRecycler = view.findViewById(R.id.RecyclerView);
        Add = view.findViewById(R.id.Add);
        ToolbarName.setText("Transactions");
        Loading = view.findViewById(R.id.Loading);
        UtilityFunctions.animateButton(getContext(), TransactionsRecycler, Add, true);
        current_month = new SimpleDateFormat("MMMM").format(new Date());


        //Fill Subjects in Spinner
        for (String month : months) {
            spinnerData.add(month);
            if (month.equals(current_month)) {
                spinnerData.add("Last 1 month");
                spinnerData.add("Last 3 months");
                spinnerData.add("Last 6 months");
                spinnerData.add("Custom");
                break;
            }
        }
        MonthSpinnerAdapter spinnerAdapter = new MonthSpinnerAdapter(getContext(), spinnerData, MonthSpinnerAdapter.HEADER);
        MonthSpinner.setAdapter(spinnerAdapter);
        for(int i=0;i<12;i++)
        {
            if(current_month.equals(months.get(i))) {
                MonthSpinner.setSelection(i);
                break;
            }
        }

        MonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                refresh();
            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddExpenseFragment addExpenseFragment = new AddExpenseFragment();
                addExpenseFragment.setData(null, false, TransactionsFragment.this);
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.Frame_Container, addExpenseFragment, "ADD").addToBackStack(null).commit();
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        refresh();

        return view;
    }

    void refresh()
    {
        TransactionData transactionData = new TransactionData(getContext(), TransactionsRecycler, Loading);
        transactionData.setSpinnerSelection(spinnerData.get(MonthSpinner.getSelectedItemPosition()));
    }

}
